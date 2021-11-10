package sv.gob.bfa.entradasvarias.model;

import java.math.BigDecimal;

import sv.gob.bfa.core.model.Respuesta;

public class EntradasVariasRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	private String cuentaTransaccion;
	private Integer numDocumentoTran;
	private String codCajero;
	private String nomCajero;
	private Integer codOficinaTran;
	private String nomOficinaTran;
	private String codPantalla;
	private Integer fechaReal;
	private Integer horaSistema;
	private BigDecimal valorEfectivo;
	private String codCompania;
	private Integer numTran;
	private String nombreCompletoCliente;
	private String codCliente;
	private String numDocumentoCliente;
	private String nombreDocumentoCliente;
	private String nombrePaisDestino;
	
	public String getNombrePaisDestino() {
		return nombrePaisDestino;
	}

	public void setNombrePaisDestino(String nombrePaisDestino) {
		this.nombrePaisDestino = nombrePaisDestino;
	}

	public String getCodCajero() {
		return codCajero;
	}

	public String getNomCajero() {
		return nomCajero;
	}

	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}

	public String getNomOficinaTran() {
		return nomOficinaTran;
	}

	public String getCodPantalla() {
		return codPantalla;
	}

	public Integer getFechaReal() {
		return fechaReal;
	}

	public Integer getHoraSistema() {
		return horaSistema;
	}

	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public String getCodCompania() {
		return codCompania;
	}

	public Integer getNumTran() {
		return numTran;
	}

	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}

	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}

	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}

	public void setNomOficinaTran(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
	}

	public void setCodPantalla(String codPantalla) {
		this.codPantalla = codPantalla;
	}

	public void setFechaReal(Integer fechaReal) {
		this.fechaReal = fechaReal;
	}

	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}

	public void setCodCompania(String codCompania) {
		this.codCompania = codCompania;
	}

	public void setNumTran(Integer numTran) {
		this.numTran = numTran;
	}

	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}

	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}

	public String getCuentaTransaccion() {
		return cuentaTransaccion;
	}

	public void setCuentaTransaccion(String cuentaTransaccion) {
		this.cuentaTransaccion = cuentaTransaccion;
	}
	
	public String getNombreCompletoCliente() {
		return nombreCompletoCliente;
	}

	public void setNombreCompletoCliente(String nombreCompletoCliente) {
		this.nombreCompletoCliente = nombreCompletoCliente;
	}

	public String getCodCliente() {
		return codCliente;
	}

	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}

	public String getNumDocumentoCliente() {
		return numDocumentoCliente;
	}

	public void setNumDocumentoCliente(String numDocumentoCliente) {
		this.numDocumentoCliente = numDocumentoCliente;
	}

	public String getNombreDocumentoCliente() {
		return nombreDocumentoCliente;
	}

	public void setNombreDocumentoCliente(String nombreDocumentoCliente) {
		this.nombreDocumentoCliente = nombreDocumentoCliente;
	}

	@Override
	public String toString() {
		return "EntradasVariasRespuesta [cuentaTransaccion=" + cuentaTransaccion + ", numDocumentoTran="
				+ numDocumentoTran + ", codCajero=" + codCajero + ", nomCajero=" + nomCajero + ", codOficinaTran="
				+ codOficinaTran + ", nomOficinaTran=" + nomOficinaTran + ", codPantalla=" + codPantalla
				+ ", fechaReal=" + fechaReal + ", horaSistema=" + horaSistema + ", valorEfectivo=" + valorEfectivo
				+ ", codCompania=" + codCompania + ", numTran=" + numTran + ", nombreCompletoCliente="
				+ nombreCompletoCliente + ", codCliente=" + codCliente + ", numDocumentoCliente=" + numDocumentoCliente
				+ ", nombreDocumentoCliente=" + nombreDocumentoCliente + "]";
	}
	
}
