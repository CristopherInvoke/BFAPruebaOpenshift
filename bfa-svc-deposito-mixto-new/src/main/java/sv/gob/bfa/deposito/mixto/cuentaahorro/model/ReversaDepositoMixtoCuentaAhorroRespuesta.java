package sv.gob.bfa.deposito.mixto.cuentaahorro.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

public class ReversaDepositoMixtoCuentaAhorroRespuesta extends Respuesta {
	
	
	private static final long serialVersionUID = 1L;

	private String 			nombreCompletoCliente;
	private String 			codCliente;
	private String 			numDocumentoCliente;
	private String 			lugarExpedicion;
	private Integer 		fechaExpedicion;
	private String 			nomCajero;
	private String 			nomOficinaTran;
	private String 			codPantalla;
	private Integer 		fechaSistema;
	private Integer 		fechaReal;
	private Integer 		fechaRelativa;
	private Integer 		horaSistema;
	private Integer 		numTran;
	private BigDecimal 		valorEfectivo;
	private BigDecimal 		valorChequesPropios;
	private BigDecimal 		valorChequesAjenos;
	private BigDecimal 		valorChequesExt;
	private BigDecimal 		valorMovimiento;	
	private String 			nombreDocumentoCliente;
	private BigDecimal  	saldoActualCuenta;
	private BigDecimal  	saldoAnteriorCuenta;
	private List<Cheque> 	cheques;
	


	public String getCodCliente() {
		return codCliente;
	}

	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
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

	public String getNumDocumentoCliente() {
		return numDocumentoCliente;
	}

	public void setNumDocumentoCliente(String numDocumentoCliente) {
		this.numDocumentoCliente = numDocumentoCliente;
	}
	
	public Integer getNumTran() {
		return numTran;
	}

	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}

	public String getNombreCompletoCliente() {
		return nombreCompletoCliente;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public Integer getFechaExpedicion() {
		return fechaExpedicion;
	}

	public String getNomOficinaTran() {
		return nomOficinaTran;
	}

	public void setNombreCompletoCliente(String nombreCompletoCliente) {
		this.nombreCompletoCliente = nombreCompletoCliente;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public void setFechaExpedicion(Integer fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}

	public void setNomOficinaTran(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
	}

	public String getNomOficina() {
		return nomOficinaTran;
	}

	public void setNomOficina(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
	}
	
	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public BigDecimal getValorChequesPropios() {
		return valorChequesPropios;
	}

	public BigDecimal getValorChequesAjenos() {
		return valorChequesAjenos;
	}

	public BigDecimal getValorChequesExt() {
		return valorChequesExt;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}

	public void setValorChequesPropios(BigDecimal valorChequesPropios) {
		this.valorChequesPropios = valorChequesPropios;
	}

	public void setValorChequesAjenos(BigDecimal valorChequesAjenos) {
		this.valorChequesAjenos = valorChequesAjenos;
	}

	public void setValorChequesExt(BigDecimal valorChequesExt) {
		this.valorChequesExt = valorChequesExt;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}

	public String getNombreDocumentoCliente() {
		return nombreDocumentoCliente;
	}

	public void setNombreDocumentoCliente(String nombreDocumentoCliente) {
		this.nombreDocumentoCliente = nombreDocumentoCliente;
	}
	
	public BigDecimal getSaldoActualCuenta() {
		return saldoActualCuenta;
	}

	public void setSaldoActualCuenta(BigDecimal saldoActualCuenta) {
		this.saldoActualCuenta = saldoActualCuenta;
	}

	public BigDecimal getSaldoAnteriorCuenta() {
		return saldoAnteriorCuenta;
	}

	public void setSaldoAnteriorCuenta(BigDecimal saldoAnteriorCuenta) {
		this.saldoAnteriorCuenta = saldoAnteriorCuenta;
	}

	@Override
	public String toString() {
		return "ReversaDepositoMixtoCuentaAhorroRespuesta "
				+ "["
				+ "  nombreCompletoCliente=" 	+ nombreCompletoCliente
				+ ", codCliente=" 			 	+ codCliente 
				+ ", numDocumentoCliente=" 		+ numDocumentoCliente 
				+ ", lugarExpedicion=" 			+ lugarExpedicion
				+ ", fechaExpedicion=" 			+ fechaExpedicion 
				+ ", nomCajero=" 				+ nomCajero 
				+ ", nomOficinaTran="			+ nomOficinaTran 
				+ ", codPantalla=" 				+ codPantalla 
				+ ", fechaSistema="				+ fechaSistema 
				+ ", fechaReal="				+ fechaReal 
				+ ", fechaRelativa=" 			+ fechaRelativa 
				+ ", horaSistema=" 				+ horaSistema 
				+ ", numTran="					+ numTran 
				+ ", valorEfectivo=" 			+ valorEfectivo 
				+ ", valorChequesPropios=" 		+ valorChequesPropios
				+ ", valorChequesAjenos=" 		+ valorChequesAjenos 
				+ ", valorChequesExt=" 			+ valorChequesExt
				+ ", valorMovimiento=" 			+ valorMovimiento 
				+ ", nombreDocumentoCliente=" 	+ nombreDocumentoCliente
				+ ", cheques=" 					+ cheques 
				+ "]";
	}

}
