package sv.gob.bfa.deposito.mixto.cuentaahorro.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

public class DepositoMixtoCuentaAhorroRespuesta extends Respuesta {
	
	private static final long serialVersionUID = 1L;
	private String 			cuentaAhorro;
	private Integer 		numDocumentoTran;
	private String 			codCliente;
	private String 			nomCuenta;
	private Integer 		codCajero;
	private String 			nomCajero;
	private Integer 		codOficinaTran;
	private String 			nomOficinaTran;
	private String 			codPantalla;
	private Integer 		fechaSistema;
	private Integer 		fechaReal;
	private Integer 		fechaRelativa;
	private Integer 		horaSistema;
	private BigDecimal 		valorEfectivo;
	private BigDecimal 		valorChequesPropios;
	private BigDecimal 		valorChequesAjenos;
	private BigDecimal 		valorChequesExt;
	private BigDecimal 		valorMovimiento;
	private String 			nombreDocumentoPersona;
	private String 			numDocumentoPersona;
	private Integer 		numTransLavado;
	private String 			numDocumentoCliente;	
	private String 			lugarExpedicion;
	private Integer 		fechaExpedicion;
	private String 			nombrePersona;
	private Integer 		numTran;	
	private String 			nombreDocumentoCliente;
	private BigDecimal  	saldoActualCuenta;
	private BigDecimal  	saldoAnteriorCuenta;
	private List<Cheque> 	cheques;	


	public String getCuentaAhorro() {
		return cuentaAhorro;
	}

	public void setCuentaAhorro(String cuentaAhorro) {
		this.cuentaAhorro = cuentaAhorro;
	}

	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}

	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}

	public String getCodCliente() {
		return codCliente;
	}

	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}

	public String getNomCuenta() {
		return nomCuenta;
	}

	public void setNomCuenta(String nomCuenta) {
		this.nomCuenta = nomCuenta;
	}

	public Integer getCodCajero() {
		return codCajero;
	}

	public void setCodCajero(Integer codCajero) {
		this.codCajero = codCajero;
	}

	public String getNomCajero() {
		return nomCajero;
	}

	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}

	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}

	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}

	public String getNomOficinaTran() {
		return nomOficinaTran;
	}

	public void setNomOficinaTran(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
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

	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
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

	public BigDecimal getValorChequesExt() {
		return valorChequesExt;
	}

	public void setValorChequesExt(BigDecimal valorChequesExt) {
		this.valorChequesExt = valorChequesExt;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}

	public String getNombreDocumentoPersona() {
		return nombreDocumentoPersona;
	}

	public void setNombreDocumentoPersona(String nombreDocumentoPersona) {
		this.nombreDocumentoPersona = nombreDocumentoPersona;
	}

	public String getNumDocumentoPersona() {
		return numDocumentoPersona;
	}

	public void setNumDocumentoPersona(String numDocumentoPersona) {
		this.numDocumentoPersona = numDocumentoPersona;
	}

	public Integer getNumTransLavado() {
		return numTransLavado;
	}

	public void setNumTransLavado(Integer numTransLavado) {
		this.numTransLavado = numTransLavado;
	}

	public String getNumDocumentoCliente() {
		return numDocumentoCliente;
	}

	public void setNumDocumentoCliente(String numDocumentoCliente) {
		this.numDocumentoCliente = numDocumentoCliente;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public Integer getFechaExpedicion() {
		return fechaExpedicion;
	}

	public void setFechaExpedicion(Integer fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}

	public String getNombrePersona() {
		return nombrePersona;
	}

	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}

	public Integer getNumTran() {
		return numTran;
	}

	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}

	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
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
		return "DepositoMixtoCuentaAhorroRespuesta "
				+ "[cuentaAhorro=" 				+ cuentaAhorro 
				+ ", numDocumentoTran="			+ numDocumentoTran 
				+ ", codCliente=" 				+ codCliente 
				+ ", nomCuenta=" 				+ nomCuenta 
				+ ", codCajero="				+ codCajero 
				+ ", nomCajero=" 				+ nomCajero 
				+ ", codOficinaTran="			+ codOficinaTran 
				+ ", nomOficinaTran="			+ nomOficinaTran 
				+ ", codPantalla=" 				+ codPantalla 
				+ ", fechaSistema=" 			+ fechaSistema 
				+ ", fechaReal="				+ fechaReal 
				+ ", fechaRelativa=" 			+ fechaRelativa 
				+ ", horaSistema=" 				+ horaSistema 
				+ ", valorEfectivo="			+ valorEfectivo 
				+ ", valorChequesPropios=" 		+ valorChequesPropios 
				+ ", valorChequesAjenos="		+ valorChequesAjenos 
				+ ", valorChequesExt=" 			+ valorChequesExt 
				+ ", valorMovimiento=" 			+ valorMovimiento
				+ ", nombreDocumentoPersona=" 	+ nombreDocumentoPersona 
				+ ", numDocumentoPersona=" 		+ numDocumentoPersona
				+ ", numTransLavado=" 			+ numTransLavado 
				+ ", numDocumentoCliente=" 		+ numDocumentoCliente 
				+ ", lugarExpedicion="			+ lugarExpedicion 
				+ ", fechaExpedicion=" 			+ fechaExpedicion 
				+ ", nombrePersona=" 			+ nombrePersona
				+ ", numTran=" 					+ numTran 
				+ ", nombreDocumentoCliente=" 	+ nombreDocumentoCliente 
				+ ", saldoAnteriorCuenta"		+ saldoAnteriorCuenta
				+ ", saldoActualCuenta"			+ saldoActualCuenta
				+ ", cheques=" 					+ cheques
				+ "]";
	}

}
