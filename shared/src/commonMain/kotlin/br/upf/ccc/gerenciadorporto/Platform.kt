package br.upf.ccc.gerenciadorporto

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform