package sv.gob.bfa.pagoprestamo.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Respuesta;

public class ReversaPagoPrestamoRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	private String nombreCompletoCliente;
	private String codCliente;
	private String duiCliente;
	private String lugarExpedicion;
	private String nombreDocumentoCliente;
	private Integer fechaExpedicion;
	private String codCajero;
	private String nomCajero;
	private String nomOficinaTran;
	private String codPantalla;
	private Integer fechaSistema;
	private Integer fechaReal;
	private Integer fechaRelativa;
	private Integer horaSistema;
	private String cuentaPrestamo;
	private Integer codOficinaTran;
	private Integer numTransLavado;
	private BigDecimal valorMovimiento;
	private Integer numTran;
	private List<Cheque> cheques;
	
	
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
	public String getLugarExpedicion() {
		return lugarExpedicion;
	}
	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}
	public String getNombreDocumentoCliente() {
		return nombreDocumentoCliente;
	}
	public void setNombreDocumentoCliente(String nombreDocumentoCliente) {
		this.nombreDocumentoCliente = nombreDocumentoCliente;
	}
	public Integer getFechaExpedicion() {
		return fechaExpedicion;
	}
	public void setFechaExpedicion(Integer fechaExpedicion) {
		this.fechaExpedicion = fechaExpedicion;
	}
	public String getCodCajero() {
		return codCajero;
	}
	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
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
	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}
	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
	}
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}
	public Integer getNumTransLavado() {
		return numTransLavado;
	}
	public void setNumTransLavado(Integer numTransLavado) {
		this.numTransLavado = numTransLavado;
	}
	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}
	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
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
	public String getNomCajero() {
		return nomCajero;
	}
	public void setNombreCompletoCliente(String nombreCompletoCliente) {
		this.nombreCompletoCliente = nombreCompletoCliente;
	}
	public void setNomCajero(String nomCajero) {
		this.nomCajero = nomCajero;
	}
	public String getNomOficinaTran() {
		return nomOficinaTran;
	}
	public void setNomOficinaTran(String nomOficinaTran) {
		this.nomOficinaTran = nomOficinaTran;
	}
	public List<Cheque> getCheques() {
		return cheques;
	}
	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	@Override
	public String toString() {
		return "ReversaPagoPrestamoRespuesta [nombreCompletoCliente=" + nombreCompletoCliente + ", codCliente="
				+ codCliente + ", duiCliente=" + duiCliente + ", lugarExpedicion=" + lugarExpedicion
				+ ", nombreDocumentoCliente=" + nombreDocumentoCliente + ", fechaExpedicion=" + fechaExpedicion
				+ ", codCajero=" + codCajero + ", nomCajero=" + nomCajero + ", nomOficinaTran=" + nomOficinaTran
				+ ", codPantalla=" + codPantalla + ", fechaSistema=" + fechaSistema + ", fechaReal=" + fechaReal
				+ ", fechaRelativa=" + fechaRelativa + ", horaSistema=" + horaSistema + ", cuentaPrestamo="
				+ cuentaPrestamo + ", codOficinaTran=" + codOficinaTran + ", numTransLavado=" + numTransLavado
				+ ", valorMovimiento=" + valorMovimiento + ", numTran=" + numTran + ", cheques=" + cheques + "]";
	}

}
