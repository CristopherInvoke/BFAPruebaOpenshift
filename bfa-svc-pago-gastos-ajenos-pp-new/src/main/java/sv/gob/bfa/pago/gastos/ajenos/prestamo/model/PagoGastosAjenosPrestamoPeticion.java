package sv.gob.bfa.pago.gastos.ajenos.prestamo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Peticion;
import sv.gob.bfa.core.model.Cheque;

/**
 * Clase correspondiente a la petici&oacuten esperada por el servicio
 * Deposito Mixto de Cuenta Corriente. Contiene los par&aacutemetros que el servicio espera.
 */
@XmlRootElement(name="PagoGastosAjenosPrestamoPeticion")
public class PagoGastosAjenosPrestamoPeticion extends Peticion{
	
	private static final long serialVersionUID = 1L;

	/**	
	 * Código de la transacci&oacuten
	 */
	private Integer codTran;

	/**
	 * N&uacutemero de documento de la transacci&oacuten
	 */
	private Integer numDocumentoTran;
	
	/**
	 * N&uacutemero de la cuenta corriente. Compuesto por los siguientes campos:
	 * codProducto
	 * codOficina
	 * numCuenta
	 * digitoVerificador 
	 */
	private String cuentaPrestamo;
	
	
	
	/**
	 * C&oacutedigo de la oficina donde se realiza la transacci&oacuten
	*/
	private Integer codOficinaTran;
	
	/**
	 * Monto en efectivo del depósito
	 */
	private BigDecimal valorEfectivo;
	
	/**
	 * Monto en valores en el depósito (cheques propios, ajenos)
	 */
	private BigDecimal valorCheques;
	/**
	 * C&oacutedigo de Tipo de Documento de la persona que realiza la transacci&oacuten
	 */
	private BigDecimal valorChequesPropios;
	
	private BigDecimal valorChequesAjenos;

	
	/**
	 * Valor total del movimiento
	*/
	private BigDecimal valorMovimiento;
	
	
	/**
	 * C&oacutedigo de terminal donde se realiza la transacci&oacuten.
	*/
	private Integer codTerminal;
	
	
	/**
	 * C&oacutedigo de cajero que realiza la transacci&oacuten.
	*/
	private String codCajero;
	
	/**
	 * N&uacutemero de caja física en la que trabaja el cajero.
	*/
	private Integer numCaja;

	/**
	 * Se&ntildeal de supervisor.  Valores posibles son 1=Si , 2=No
	*/
	
	private Integer senSupervisor;
	private Integer tipDocumentoPersona;

	private String numDocumentoPersona;
	
	private String nombrePersona;
	
	private List<Cheque> cheques;
	
		
	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}
	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
	}

	
	public String getNombrePersona() {
		return nombrePersona;
	}
	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}


	public Integer getTipDocumentoPersona() {
		return tipDocumentoPersona;
	}
	public void setTipDocumentoPersona(Integer tipDocumentoPersona) {
		this.tipDocumentoPersona = tipDocumentoPersona;
	}
	public String getNumDocumentoPersona() {
		return numDocumentoPersona;
	}
	public void setNumDocumentoPersona(String numDocumentoPersona) {
		this.numDocumentoPersona = numDocumentoPersona;
	}
	
	public Integer getCodTran() {
		return codTran;
	}
	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}
	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}

	
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}
	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}
	public BigDecimal getValorCheques() {
		return valorCheques;
	}
	public void setValorCheques(BigDecimal valorCheques) {
		this.valorCheques = valorCheques;
	}
	
	public BigDecimal getValorChequesPropios() {
		return valorChequesPropios;
	}

	public void setValorChequesPropios(BigDecimal valorChequesPropios) {
		this.valorChequesPropios = valorChequesPropios;
	}

	public BigDecimal getValorChequesAjenos() {
		return valorChequesAjenos;
	}

	public void setValorChequesAjenos(BigDecimal valorChequesAjenos) {
		this.valorChequesAjenos = valorChequesAjenos;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}
	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}
	public Integer getCodTerminal() {
		return codTerminal;
	}
	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}
	public String getCodCajero() {
		return codCajero;
	}
	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}
	public Integer getNumCaja() {
		return numCaja;
	}
	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}
	
	public List<Cheque> getCheques() {
		return cheques;
	}
	
	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	public Integer getSenSupervisor() {
		return senSupervisor;
	}
	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}
	
}
