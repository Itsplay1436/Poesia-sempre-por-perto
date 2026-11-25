package br.com.pam

/** Regras compartilháveis com a implementação Expo. */
data class Filters(var title: String = "", var author: String = "", var lines: String = "", var min: String = "", var max: String = "") : java.io.Serializable {
    fun validate(): String? {
        val low = min.toIntOrNull()
        val high = max.toIntOrNull()
        if ((min.isNotBlank() && (low == null || low < 0)) || (max.isNotBlank() && (high == null || high < 0))) return "Informe uma quantidade de versos válida."
        if (low != null && high != null && low > high) return "O mínimo de versos não pode ser maior que o máximo."
        return null
    }
}
