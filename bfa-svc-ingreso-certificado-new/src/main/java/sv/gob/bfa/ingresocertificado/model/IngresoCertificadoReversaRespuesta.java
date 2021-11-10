package sv.gob.bfa.ingresocertificado.model;
import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

public class IngresoCertificadoReversaRespuesta extends Respuesta {

	private static final long serialVersionUID = -7053945088302856636L;

	private Integer 	codigo;
	private String 		descripcion;
	private String 		cuentaDeposito;
	private Integer 	numDocumentoTran;
	private String 		codCliente;
	private String 		nombreCliente;
	private String 		codCajero;
	private String 		nomCajero;
	private Integer 	codOficinaTran;
	private String 		nomOficinaTran;
	private String 		codPantalla;
	private Integer 	fechaSistema;
	private Integer 	fechaReal;
	private Integer 	horaSistema;
	private BigDecimal 	valorEfectivo;
	private BigDecimal  valorChequesPropios;
	private BigDecimal  valorChequesAjenos;
	private BigDecimal  valorChequesExt;
	private BigDecimal 	valorCheques;
	private BigDecimal 	valorMovimiento;
	private String 		nomTipDocumentoCliente;
	private String 		numDocumentoCliente;
	private String 		lugarExpedicion;
	private Integer 	fechaExpedicion;
	private Integer 	numReversa;
	private List<Cheque> cheques;

	public IngresoCertificadoReversaRespuesta() {
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCuentaDeposito() {
		return cuentaDeposito;
	}

	public void setCuentaDeposito(String cuentaDeposito) {
		this.cuentaDeposito = cuentaDeposito;
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

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getCodCajero() {
		return codCajero;
	}

	public void setCodCajero(String codCajero) {
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

	public String getNomTipDocumentoCliente() {
		return nomTipDocumentoCliente;
	}

	public void setNomTipDocumentoCliente(String nomTipDocumentoCliente) {
		this.nomTipDocumentoCliente = nomTipDocumentoCliente;
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
	
	public Integer getNumReversa() {
		return numReversa;
	}

	public void setNumReversa(Integer numReversa) {
		this.numReversa = numReversa;
	}

	public List<Cheque> getCheques() {
		return cheques;
	}


	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	@Override
	public String toString() {
		return "IngresoCertificadoReversaRespuesta "
				+ "["
				+ "  codigo=" 					+ codigo 
				+ ", descripcion=" 				+ descripcion
				+ ", cuentaDeposito=" 			+ cuentaDeposito 
				+ ", numDocumentoTran=" 		+ numDocumentoTran 
				+ ", codCliente="				+ codCliente 
				+ ", nombreCliente=" 			+ nombreCliente 
				+ ", codCajero=" 				+ codCajero 
				+ ", nomCajero="				+ nomCajero 
				+ ", codOficinaTran=" 			+ codOficinaTran 
				+ ", nomOficinaTran=" 			+ nomOficinaTran
				+ ", codPantalla=" 				+ codPantalla 
				+ ", fechaSistema=" 			+ fechaSistema
				+ ", fechaReal=" 				+ fechaReal	
				+ ", horaSistema=" 				+ horaSistema 
				+ ", valorEfectivo=" 			+ valorEfectivo 
				+ ", valorCheques=" 			+ valorCheques
				+ ", valorChequesPropios="  	+ valorChequesPropios
				+ ", valorChequesAjenos="   	+ valorChequesAjenos 
				+ ", valorChequesExt="      	+ valorChequesExt
				+ ", valorMovimiento=" 			+ valorMovimiento 
				+ ", nomTipDocumentoCliente=" 	+ nomTipDocumentoCliente 
				+ ", numDocumentoCliente=" 		+ numDocumentoCliente
				+ ", lugarExpedicion="			+ lugarExpedicion 
				+ ", fechaExpedicion="			+ fechaExpedicion 
				+ ", numReversa="				+ numReversa
				+ ", cheques=" 					+ cheques
				+ "]";
	}
}
