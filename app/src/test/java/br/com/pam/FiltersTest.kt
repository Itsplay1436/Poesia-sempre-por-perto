package br.com.pam
import org.junit.Assert.*
import org.junit.Test
class FiltersTest {
    @Test fun intervaloInvertido() { assertNotNull(Filters(min="20",max="10").validate()) }
    @Test fun limiteUnico() { assertNull(Filters(min="14").validate()); assertNull(Filters(max="14").validate()) }
    @Test fun intervaloInclusivo() { assertNull(Filters(min="14",max="14").validate()) }
    @Test fun entradaInvalida() { assertNotNull(Filters(min="-1").validate()); assertNotNull(Filters(max="9999999999999999").validate()); assertNotNull(Filters(min="abc").validate()) }
    @Test fun semFiltro() { assertNull(Filters().validate()) }
}
