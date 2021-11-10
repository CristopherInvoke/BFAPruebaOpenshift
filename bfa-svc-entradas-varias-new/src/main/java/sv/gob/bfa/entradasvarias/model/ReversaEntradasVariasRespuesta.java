package sv.gob.bfa.entradasvarias.model;

import java.math.BigDecimal;

import sv.gob.bfa.core.model.Respuesta;

public class ReversaEntradasVariasRespuesta extends Respuesta{
	
	private static final long serialVersionUID = 1L;
	
	private String cuentaTransaccion;
	private Integer numDocumentoTran;
	private String codCajero;
	private String nomCajero;
	private Integer codOficinaTran;
	private String nomOficinaTran;
	private String codPantalla;
	private Integer horaSistema;
	private BigDecimal valorEfectivo;
	private BigDecimal valorMovimiento;
	private String nombreCompletoCliente;
	private String codCliente;
	private String numDocumentoCliente;
	private String nombreDocumentoCliente;
	
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}
	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
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
	public Integer getHoraSistema() {
		return horaSistema;
	}
	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}
	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}
	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}
	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
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
		return "ReversaEntradasVariasRespuesta [cuentaTransaccion=" + cuentaTransaccion + ", numDocumentoTran="
				+ numDocumentoTran + ", codCajero=" + codCajero + ", nomCajero=" + nomCajero + ", codOficinaTran="
				+ codOficinaTran + ", nomOficinaTran=" + nomOficinaTran + ", codPantalla=" + codPantalla
				+ ", horaSistema=" + horaSistema + ", valorEfectivo=" + valorEfectivo + ", valorMovimiento=" + valorMovimiento + ", nombreCompletoCliente="
				+ nombreCompletoCliente + ", codCliente=" + codCliente + ", numDocumentoCliente=" + numDocumentoCliente
				+ ", nombreDocumentoCliente=" + nombreDocumentoCliente + "]";
	}
	
}
