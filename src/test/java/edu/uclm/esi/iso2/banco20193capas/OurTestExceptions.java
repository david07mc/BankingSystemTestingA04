package edu.uclm.esi.iso2.banco20193capas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoAutorizadoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoEncontradoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaYaCreadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OurTestExceptions extends TestCase {
    private Cuenta cuentaPepe, cuentaAna;
    private Cliente pepe, ana;
    
    @Before
    public void setUp() {
        Manager.getMovimientoDAO().deleteAll();
        Manager.getMovimientoTarjetaCreditoDAO().deleteAll();
        Manager.getTarjetaCreditoDAO().deleteAll();
        Manager.getTarjetaDebitoDAO().deleteAll();
        Manager.getCuentaDAO().deleteAll();
        Manager.getClienteDAO().deleteAll();

        this.pepe = new Cliente("12345X", "Pepe", "Pérez"); this.pepe.insert();
        this.ana = new Cliente("98765F", "Ana", "López"); this.ana.insert();
        this.cuentaPepe = new Cuenta(1); this.cuentaAna = new Cuenta(2);
        
        try {
            this.cuentaPepe.addTitular(pepe); this.cuentaPepe.insert(); this.cuentaPepe.ingresar(1000);
            this.cuentaAna.addTitular(ana); this.cuentaAna.insert(); this.cuentaAna.ingresar(5000);
        } catch (Exception e) {
            fail("Excepción inesperada en setUp(): " + e);
        }
        
    }
    
    @Test
    public void testEmitirTCClienteNoExistente() {
        @SuppressWarnings("unused")
        TarjetaCredito tcAna;
        
        String DNINoExistente = "0000A";
        
        try {
            tcAna = this.cuentaAna.emitirTarjetaCredito(DNINoExistente, 10000);
            fail("Expected ClienteNoEncontradoException");
        } catch (ClienteNoEncontradoException e) {
        } catch (Exception e) {
            fail("Excepción inesperada " + e);
        }
    }
    
    @Test
    public void testEmitirTCClienteNoAutorizado() {
        @SuppressWarnings("unused")
        TarjetaCredito tcAna;
        
        try {
            tcAna = this.cuentaAna.emitirTarjetaCredito(pepe.getNif(), 10000);
            fail("Expected ClienteNoAutorizadoException");
        } catch (ClienteNoAutorizadoException e) {
        } catch (Exception e) {
            fail("Excepción inesperada " + e);
        }
    }
    
    @Test
    public void testIngresarCantidadNegativa() {
        try {
            this.cuentaPepe.ingresar(-1000);
            fail("Esperaba ImporteInvalidoException");
        } catch (ImporteInvalidoException e) {
        } catch (Exception e) {
            fail("Esperaba ImporteInvalidoException");
        } 
    }
    
    @Test
    public void testAñadirTitularCuentaCreada() {
        try {
            this.cuentaPepe.addTitular(this.ana);
            fail("Esperaba CuentaYaCreadaException");
        } catch (CuentaYaCreadaException e){
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        } 
    }
    
}

    