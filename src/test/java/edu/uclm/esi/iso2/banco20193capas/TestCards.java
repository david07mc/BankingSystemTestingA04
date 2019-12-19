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
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TarjetaBloqueadaException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaDebito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCards extends TestCase {
    private Cuenta cuentaPepe, cuentaAna;
    private Cliente pepe, ana;
    private TarjetaDebito tdPepe, tdAna;
    private TarjetaCredito tcPepe, tcAna;

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
            this.tcPepe = this.cuentaPepe.emitirTarjetaCredito(pepe.getNif(), 2000);
            this.tcAna = this.cuentaAna.emitirTarjetaCredito(ana.getNif(), 10000);
            this.tdPepe = this.cuentaPepe.emitirTarjetaDebito(pepe.getNif());
            this.tdAna = this.cuentaAna.emitirTarjetaDebito(ana.getNif());

            this.tcPepe.cambiarPin(tcPepe.getPin(), 1234);
            this.tcAna.cambiarPin(tcAna.getPin(), 1234);
            this.tdPepe.cambiarPin(tdPepe.getPin(), 1234);
            this.tdAna.cambiarPin(tdAna.getPin(), 1234);
        }
        catch (Exception e) {
            fail("Excepción inesperada en setUp(): " + e);
        }
    }
    
    @Test
    public void testTDComprar() {
        double saldoAntiguo = cuentaAna.getSaldo();
        
        try {
            tdAna.comprar(1234, 500);
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
        
        assertTrue(saldoAntiguo-500 == cuentaAna.getSaldo());
        
    }
    
    @Test
    public void testTDComprarPorInternet() {
        double saldoAntiguo = cuentaAna.getSaldo();
        double importe = cuentaAna.getSaldo()-2;
        try {
            int token = tdAna.comprarPorInternet(1234, importe);
            tdAna.confirmarCompraPorInternet(token);
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
        
        assertTrue(saldoAntiguo-importe == cuentaAna.getSaldo());
        
    }
    
    @Test
    public void testTDCambiarPINValido() {
        try {
            tdAna.cambiarPin(tdAna.getPin(), 1235);
        } catch (Exception e) {
        	fail("Excepción inesperada: " + e);
        }
    }
    
    @Test
    public void testTDCambiarPINInvalido() {
        try {
            tdAna.cambiarPin(0, 1235);
            fail("Esperaba PinInvalidoException");
        } catch (PinInvalidoException e) {
        }
    }
    
    @Test
    public void testTDBloqueo() {
        try {
            tdAna.comprar(0, 500);
            fail("Esperaba PinInvalidoException");
        } catch (PinInvalidoException e) {
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
        
        try {
            tdAna.comprar(0, 500);
            fail("Esperaba PinInvalidoException");
        } catch (PinInvalidoException e) {
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
        
        try {
            tdAna.comprar(0, 500);
            fail("Esperaba PinInvalidoException");
        } catch (PinInvalidoException e) {
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
        
        try {
            tdAna.comprar(1234, 500);
            fail("Esperaba TarjetaBloqueadaException");
        } catch (TarjetaBloqueadaException e) {
        } catch (Exception e) {
            fail("Excepción inesperada: " + e);
        }
    }
    
	@Test
	public void testRetiradaCredito() {
		try {
			this.tcPepe.sacarDinero(tcPepe.getPin(), tcPepe.getCreditoDisponible()-2);
		} catch (Exception e) {
			fail("Unexpected Exception " + e);
		}
	}
    
	@Test
	public void testRetiradaCreditoSaldoInsuficiente() {
		try {
			this.tcPepe.sacarDinero(tcPepe.getPin(), tcPepe.getCreditoDisponible()+2);
			fail("Esperaba SaldoInsuficienteException");
		} catch (SaldoInsuficienteException e) {
		} catch (Exception e) {
			fail("Unexpected Exception " + e);
		}
	}
	
	@Test
	public void testRetiradaCreditoSaldoImporteNegativo() {
		try {
			this.tcPepe.sacarDinero(tcPepe.getPin(), -1);
			fail("Esperaba ImporteInvalidoException");
		} catch (ImporteInvalidoException e) {
		} catch (Exception e) {
			fail("Unexpected Exception " + e);
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
	
}

    