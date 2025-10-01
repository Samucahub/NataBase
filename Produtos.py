
class produto:
    def __init__(self, nome, tipo, quantidade):
        self.__nome = nome
        self.__tipo = tipo
        # garante não-negativo na criação
        self.__quantidade = max(0, int(quantidade or 0))

    # getters
    def getnome(self): return self.__nome
    def gettipo(self): return self.__tipo
    def getquantidade(self): return self.__quantidade

    # Quantidades (sempre não-negativas)
    def aumentarquantidade(self, valor):
        v = max(0, int(valor or 0))   # ignora números negativos
        self.__quantidade += v

    def diminuirquantidade(self, valor):
        v = max(0, int(valor or 0))   # ignora números negativos
        # nunca deixa ir abaixo de zero
        self.__quantidade = max(0, self.__quantidade - v)

    def exibirDetalhes(self):
        return print(f"Nome: {self.__nome}\n "
                     f"Tipo: {self.__tipo}\n "
                     f"Quantidade:{self.__quantidade}\n ")

    def zerar(self):
        self.__quantidade = 0

