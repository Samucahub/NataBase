package com.belasoft.natabase_alpha.utils

import com.belasoft.natabase_alpha.R

object ImageMapping {

    private val categoriaImagens = mapOf(
        "PASTELARIA" to R.drawable.pastelaria,
        "CROISSANTS" to R.drawable.croissant,
        "SALGADOS" to R.drawable.salgados,
        "TOSTAS / SANDUÍCHES" to R.drawable.sandwich,
        "REGIONAIS" to R.drawable.regionais,
        "PÃO" to R.drawable.pao
    )

    private val produtoImagens = mapOf(
        "BOLA DE BERLIM" to R.drawable.produto_bola_berlim,
        "BOLO CENOURA FATIA" to R.drawable.produto_bolo_cenoura,
        "BOLO CHOCOLATE FATIA" to R.drawable.produto_bolo_chocolate,
        "BOLO DE ARROZ" to R.drawable.produto_bolo_arroz,
        "BRIGADEIRO" to R.drawable.brigadeiro,
        "FRIPANUTS CHOCOLATE" to R.drawable.donut,
        "FRIPANUTS SUGAR" to R.drawable.donut_chocolate,
        "MINI CHAUSSON" to R.drawable.mini_chausson,
        "TRANÇA CREME E MAÇÃ" to R.drawable.tranca,
        "MUFFIN CHOCOLATE" to R.drawable.muffin_chocolate,
        "MUFFIN LIMÃO" to R.drawable.muffin_limao,
        "PAO DEUS SIMPLES" to R.drawable.pao_de_deus,
        "PASTEL DE NATA" to R.drawable.produto_pastel_nata,
        "QUEIJADA DE LEITE" to R.drawable.produto_queijada_leite,
        "QUEIJADA DE MARACUJÁ" to R.drawable.queijada_maracuja,
        "QUEIJADA LARANJA" to R.drawable.queijadas_de_laranja,
        "QUEIJADA FEIJÃO" to R.drawable.queijadas_feijao,
        "SCONE SIMPLES" to R.drawable.scone_simples,
        "TARTE MAÇÃ PREMIUM" to R.drawable.tranca,

        "CROISSANT SIMPLES" to R.drawable.produto_croissant_simples,
        "CROISSANT CHOC E AVELÃ" to R.drawable.produto_croissant_chocolate,
        "CROISSANT MULTICEREAIS" to R.drawable.produto_croissant_cereais,
        "CROISSANT MULTICEREAIS MISTO" to R.drawable.produto_croissant_cereais_misto,
        "CROISSANT MISTO" to R.drawable.produto_croissant_misto,
        "PÃO DE DEUS MISTO" to R.drawable.produto_pao_deus_misto,

        "CHAMUÇA" to R.drawable.produto_chamuca,
        "EMPANADA FRANGO" to R.drawable.produto_empada_frango,
        "CROQ CARNE" to R.drawable.produto_croq_carne,
        "EMPANADA CAPRESE" to R.drawable.empanada_caprese,
        "EMPANADA Q/F" to R.drawable.pasteis_de_queijo_fiambre,
        "EMPANADAS DE ATUM" to R.drawable.empanada_de_atum,
        "FOLHADO MISTO CARNE" to R.drawable.folhado_carne_picada,
        "NAPOLITANA MISTA" to R.drawable.napolitana,
        "PASTEIS BAC" to R.drawable.pasteis,
        "RISSOL CARNE" to R.drawable.rissol_carne,
        "RISSOL MARISCO" to R.drawable.rissol_camarao,

        "BAGUETE AMERICANA" to R.drawable.produto_baguete_americana,
        "TOSTA MISTA SALOIA" to R.drawable.produto_tosta_mista,
        "BAGUETE ATUM" to R.drawable.produto_baguete_atum,
        "BAGUETE DELICIAS" to R.drawable.produto_baguete_delicias,
        "BAGUETE PRESUNTO QUEI" to R.drawable.produto_baguete_presunto_queijo,
        "BOLA PANADO DE PORCO" to R.drawable.produto_bola_panado_porco,
        "PAO QUEIJO FRESCO" to R.drawable.produto_pao_queijo,
        "PAO SALMAO FUMADO" to R.drawable.produto_pao_salmao_fumado,
        "SD FRANGO COGUMELOS" to R.drawable.produto_sandwich_frango_cogumelos,
        "BAGUETE PRESUNTO" to R.drawable.produto_baguete_presunto,
        "BOLA 110 GRS MISTA" to R.drawable.produto_bola_mista,
        "BOLA PANADO DE PORCO (S/ Alface)" to R.drawable.produto_bola_panado_porco_sem_alface,
        "TOSTA ATUM SALOIA" to R.drawable.produto_tosta_atum,
        "TOSTA FRANGO SALOIA" to R.drawable.produto_tosta_frango,
        "TOSTA PRESUNTO/QUEIJO SALOIA" to R.drawable.produto_tosta_presunto_queijo,
        "BOLO CACO MISTO" to R.drawable.produto_bolo_caco_misto,
        "SD AMERICANA" to R.drawable.produto_sandwich_americana,

        "PÃO DE LÓ OVAR PEQ 85 GRS" to R.drawable.produto_pao_lo_ovar,
        "OVOS MOLES UND" to R.drawable.produto_ovos_moles,
        "TARTES DE AMÊNDOA UND" to R.drawable.produto_tarte_amendoa,
        "TRAVESSEIRO SINTRA" to R.drawable.produto_travesseiro,
        "PASTEIS TORRES VEDRAS UND" to R.drawable.produto_pasteis_torres_vedras,
        "PASTEIS AGUEDA UND" to R.drawable.produto_pasteis_agueda,
        "PASTEIS VOUZELA UND" to R.drawable.produto_pasteis_vouzela,
        "TORTA DE AZEITÃO UND" to R.drawable.produto_torta_azeitao,
        "QUEIJADA MADEIRENSE" to R.drawable.produto_queijada_madeirense,
        "MALASADA CREME (FRESCO)" to R.drawable.produto_malasada,
        "SALAME (FATIA)" to R.drawable.produto_salame,

        "BAGUETE" to R.drawable.produto_baguete,
        "BOLA LENHA" to R.drawable.produto_pao_lenha,
        "PÃO CEREAIS" to R.drawable.produto_pao_cereais,
        "PÃO RUSTICO FATIAS" to R.drawable.produto_pao_rustico
    )

    fun getImagemCategoria(categoria: String): Int {
        return categoriaImagens[categoria] ?: R.drawable.padariabackground_login
    }

    fun getImagemProduto(produto: String): Int {
        return produtoImagens[produto] ?: R.drawable.padariabackground_login
    }

    fun hasImagemProduto(produto: String): Boolean {
        return produtoImagens.containsKey(produto)
    }
}