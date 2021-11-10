package sv.gob.bfa.pago.gastos.ajenos.prestamo.model;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;


/**
 * Clase correspondiente a la petici&oacuten esperada por el servicio
 * Reversa Deposito Mixto de Cuenta Corriente. Contiene los par&aacutemetros que el servicio espera.
 */
@XmlRootElement(name="ReversaPagoGastosAjenosPrestamoPeticion")
public class ReversaPagoGastosAjenosPrestamoPeticion extends Peticion{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * C&oacutedigo de la transacci&oacuten.
	 */
	private Integer codTran;
	
	/**
	 * N&uacutemero de Documento a reversar
	 */
	private Integer numDocumentoReversa;
	
	
	/**
	 * N&uacutemero de la cuenta corriente. Compuesto por los siguientes campos:
	 * codProducto
	 * codOficina
	 * numCuenta
	 * digitoVerificador 
	 */
	private String cuentaPrestamo;
	
	/**
	 * Monto en efectivo del depósito.
	 */
	private BigDecimal valorEfectivo;
	
	/**
	 * Monto en valores en el depósito (cheques propios, ajenos)
	 */
	private BigDecimal valorCheques;
	/**
	 * Total valores cheques propios
	 */
	private BigDecimal valorChequesPropios;
	
	/**
	 * Total valores cheques ajenos
	 */
	private BigDecimal valorChequesAjenos;
	
	

	private BigDecimal valorMovimiento;
	
	/**
	 * C&oacutedigo de la oficina donde se realiza la transacci&oacuten.
	 */
	private Integer codOficinaTran;
	
	/**
	 * C&oacutedigo de terminal donde se realiza la transacci&oacuten.
	 */
	private Integer codTerminal;
	
	/**
	 * C&oacutedigo de cajero que realiza la transacci&oacuten.
	 */
	private String codCajero;
	
	/**
	 * N&uacutemero de caja f&iacutesica en la que trabaja el cajero.
	 */
	private Integer numCaja;
	
	/**
	 * N&uacutemero de  la transacci&oacuten a reversar
	 */
	private Integer numTran;

	private List<Cheque> cheques;
	
	public List<Cheque> getCheques() {
		return cheques;
	}
	
	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	/**
	 * Arreglo de objetos de tipo cheque.
	 */
	/**
	 * N&uacutemero de documento de la transacci&oacuten
	 */
	
	

	public Integer getCodTran() {
		return codTran;
	}
	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}
	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}
	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
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
	public void setCodTerminal(Integer codTerminalTran) {
		this.codTerminal = codTerminalTran;
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
	public Integer getNumDocumentoReversa() {
		return numDocumentoReversa;
	}
	public void setNumDocumentoReversa(Integer numDocumentoReversa) {
		this.numDocumentoReversa = numDocumentoReversa;
	}
	public Integer getNumTran() {
		return numTran;
	}
	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}
	
	
	
}
