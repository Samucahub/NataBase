# email_service.py
# Serviço de envio de e-mail para anexar o Excel completo.
# Implementação simples com SMTP Gmail (SSL). Pensado para ser reutilizado.

import os
import smtplib
from email.message import EmailMessage
from pathlib import Path
from typing import List, Optional


class EmailService:
    """
    Serviço de envio de e-mails.
    - Por padrão usa Gmail SMTP (SSL, porta 465).
    - Remetente/destinatários podem vir de variáveis de ambiente ou do bloco DEFAULTS.
    """

    # >>> CONFIGURE AQUI <<<
    DEFAULTS = {
        # Remetente Gmail (de preferência uma conta técnica)
        "SMTP_HOST": "smtp.gmail.com",
        "SMTP_PORT_SSL": 465,
        "SENDER_EMAIL": os.getenv("GMAIL_USER", "relatorioloja012@gmail.com"),
        # Senha de APP do Gmail (NÃO a senha normal). Pode vir por env: GMAIL_APP_PASSWORD
        "SENDER_APP_PASSWORD": os.getenv("GMAIL_APP_PASSWORD", "cwvt qgcg etrd ydzw"),
        # Lista de destinatários separada por vírgula via env (GMAIL_TO), ou define abaixo:
        "TO_ADDRESSES": [
            addr.strip() for addr in os.getenv("GMAIL_TO", "rdonada9@gmail.com").split(",")
        ],
    }

    def __init__(
        self,
        sender_email: Optional[str] = None,
        sender_app_password: Optional[str] = None,
        to_addresses: Optional[List[str]] = None,
        smtp_host: Optional[str] = None,
        smtp_port_ssl: Optional[int] = None,
    ):
        cfg = self.DEFAULTS
        self.smtp_host = smtp_host or cfg["SMTP_HOST"]
        self.smtp_port_ssl = smtp_port_ssl or cfg["SMTP_PORT_SSL"]
        self.sender_email = sender_email or cfg["SENDER_EMAIL"]
        self.sender_app_password = sender_app_password or cfg["SENDER_APP_PASSWORD"]
        self.to_addresses = to_addresses or cfg["TO_ADDRESSES"]

        if not self.sender_email or not self.sender_app_password:
            raise ValueError("Remetente e senha de app do Gmail não configurados.")

        if not self.to_addresses:
            raise ValueError("Nenhum destinatário configurado.")

    def _build_message(self, subject: str, body: str, filepath: str) -> EmailMessage:
        p = Path(filepath)
        if not p.exists() or not p.is_file():
            raise FileNotFoundError(f"Arquivo para envio não encontrado: {filepath}")

        msg = EmailMessage()
        msg["Subject"] = subject
        msg["From"] = self.sender_email
        msg["To"] = ", ".join(self.to_addresses)
        msg.set_content(body)

        # Anexo: Excel
        with p.open("rb") as f:
            data = f.read()
        # MIME para .xlsx
        msg.add_attachment(
            data,
            maintype="application",
            subtype="vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            filename=p.name,
        )
        return msg

    def send_excel(self, filepath: str, subject: str, body: str):
        """
        Envia o Excel como anexo.
        """
        msg = self._build_message(subject=subject, body=body, filepath=filepath)

        with smtplib.SMTP_SSL(self.smtp_host, self.smtp_port_ssl) as smtp:
            smtp.login(self.sender_email, self.sender_app_password)
            smtp.send_message(msg)
