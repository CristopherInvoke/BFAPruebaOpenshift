package sv.gob.bfa.pago.gastos.ajenos.prestamo.model;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

/**
 * Clase correspondiente a la respuesta que el servicio
 * Deposito Mixto Cuenta corriente debe retornar.
 */
@XmlRootElement(name="PagoGastosAjenosPrestamoRespuesta")
public class PagoGastosAjenosPrestamoRespuesta extends Respuesta{
	
	private static final long serialVersionUID = 1L;
	/**	
	 * Código de la transacci&oacuten
	 */
	private Integer codTran;

	/**
	 * N&uacutemero de documento de la transacci&oacuten
	 */
	private String nombreCompletoCliente;
	
	public String getNombreCompletoCliente() {
		return nombreCompletoCliente;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public String getNombreDocumentoCliente() {
		return nombreDocumentoCliente;
	}

	public Integer getFechaExpedicion() {
		return fechaExpedicion;
	}

	public void setNombreCompletoCliente(String nombreCompletoCliente) {
		this.nombreCompletoCliente = nombreCompletoCliente;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public void setNombreDocumentoCliente(String nombreDocumentoCliente) {
		this.nombreDocumentoCliente = nombreDocumentoCliente;
	}

	public void setFechaExpedicion(Integer fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}
	private String lugarExpedicion;
	
	private String nombreDocumentoCliente;
	
	private Integer fechaExpedicion;

	
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
	 * C&oacutedigo del cliente
	 */
	private String codCliente;
	
	/**
	 * N&uacutemero del DUI del cliente
	 */
	private String duiCliente;
	
	
	/**
	 * Codigo de cajero
	 */
	private String codCajero;
	
	/**
	 * Monto en efectivo del depósito
	 */
	private BigDecimal valorEfectivo;
//	
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
	
	private Integer numTran;
	
	private String valorTotalCobroEnLetrasRecibo;
	
	private String pnoGasto1;
	
	private String pnoGasto2;
	private String pnoGasto3;
	private String pnoGasto4;
	private String pnoGasto5;
	
	private List<Cheque> cheques;
	
	public List<Cheque> getCheques() {
		return cheques;
	}
	
	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}
	public BigDecimal getValorCheques() {
		return valorCheques;
	}
	public BigDecimal getValorChequesPropios() {
		return valorChequesPropios;
	}
	public BigDecimal getValorChequesAjenos() {
		return valorChequesAjenos;
	}
	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}
	public void setValorCheques(BigDecimal valorCheques) {
		this.valorCheques = valorCheques;
	}
	public void setValorChequesPropios(BigDecimal valorChequesPropios) {
		this.valorChequesPropios = valorChequesPropios;
	}
	public void setValorChequesAjenos(BigDecimal valorChequesAjenos) {
		this.valorChequesAjenos = valorChequesAjenos;
	}
	public String getCodCajero() {
		return codCajero;
	}
	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}
	/**
	 * Nombre de cajero
	 */
	private String nomCajero;
	
	/**
	 * Nombre de la oficina donde se realiza la transacci&oacuten
	 */
	private String nomOficinaTran;
	
	/**
	 * C&oacutedigo de pantalla 
	 */
	private String codPantalla;
	
	/**
	 * Fecha de sistema
	 */
	private Integer fechaSistema;
	
	/**
	 * Fecha real
	 */
	private Integer fechaReal;
	
	/**
	 * Fecha relativa
	 */
	private Integer fechaRelativa;
	
	/**
	 * Hora del sistema
	 */
	private Integer horaSistema;

	private BigDecimal valorMovimiento;
	
	
	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}
	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}
	public String getCodCliente() {
		return codCliente;
	}
	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}
	public String getDuiCliente() {
		return duiCliente;
	}
	public void setDuiCliente(String duiCliente) {
		this.duiCliente = duiCliente;
	}
	
	
	public String getNomCajero() {
		return nomCajero;
	}
	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}
	public String getCodPantalla() {
		return codPantalla;
	}
	public void setCodPantalla(String codPantalla) {
		this.codPantalla = codPantalla;
	}
	public Integer getFechaSistema() {
		return fechaSistema;
	}
	public void setFechaSistema(Integer fechaSistema) {
		this.fechaSistema = fechaSistema;
	}
	public Integer getFechaReal() {
		return fechaReal;
	}
	public void setFechaReal(Integer fechaReal) {
		this.fechaReal = fechaReal;
	}
	public Integer getFechaRelativa() {
		return fechaRelativa;
	}
	public void setFechaRelativa(Integer fechaRelativa) {
		this.fechaRelativa = fechaRelativa;
	}
	public Integer getHoraSistema() {
		return horaSistema;
	}
	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}
	public Integer getNumTran() {
		return numTran;
	}
	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}
	public String getValorTotalCobroEnLetrasRecibo() {
		return valorTotalCobroEnLetrasRecibo;
	}
	public void setValorTotalCobroEnLetrasRecibo(String valorTotalCobroEnLetrasRecibo) {
		this.valorTotalCobroEnLetrasRecibo = valorTotalCobroEnLetrasRecibo;
	}
	public Integer getCodTran() {
		return codTran;
	}
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}
	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}
	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}
	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
	}
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}

	public String getPnoGasto1() {
		return pnoGasto1;
	}

	public String getPnoGasto2() {
		return pnoGasto2;
	}

	public String getPnoGasto3() {
		return pnoGasto3;
	}

	public String getPnoGasto4() {
		return pnoGasto4;
	}

	public String getPnoGasto5() {
		return pnoGasto5;
	}

	public void setPnoGasto1(String pnoGasto1) {
		this.pnoGasto1 = pnoGasto1;
	}

	public void setPnoGasto2(String pnoGasto2) {
		this.pnoGasto2 = pnoGasto2;
	}

	public void setPnoGasto3(String pnoGasto3) {
		this.pnoGasto3 = pnoGasto3;
	}

	public void setPnoGasto4(String pnoGasto4) {
		this.pnoGasto4 = pnoGasto4;
	}

	public void setPnoGasto5(String pnoGasto5) {
		this.pnoGasto5 = pnoGasto5;
	}
	
}
