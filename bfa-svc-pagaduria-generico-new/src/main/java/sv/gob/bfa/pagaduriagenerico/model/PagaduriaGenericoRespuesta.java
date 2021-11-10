package sv.gob.bfa.pagaduriagenerico.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Respuesta;

@XmlRootElement(name="PagaduriaGenericoRespuesta")
public class PagaduriaGenericoRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	private Integer fechaTransaccion;
	private String numTran;
	private String nomAgencia;
	private Integer horaSistema;
	private String numIdentificacionCliente;
	private String periodoPago;
	private String descTipoDocumento;
	private String impresionFISDL;
	private String dui;
	private String nomCliente;
	private String convenio;
	private String tipoDocumento;
	private String codPlanilla;
	private String programaFISDL;
	
	private Integer codColector;
	private Integer codTipoDocumento;
	private Integer codOficinaTran;
	private Integer codTran;
	private Integer numDocumentoTran;
	private BigDecimal valorMovimiento;
	private String camposFormulario;
	private BigDecimal valorEfectivo;
	private Integer codTerminal;
	private String codCajero;
	private String nomCajero;
	private String codPantalla;
	private Integer codCausal;
	private Integer codSubCausal;
	private Long glbDtime;
	
	public Integer getFechaTransaccion() {
		return fechaTransaccion;
	}
	public String getNumTran() {
		return numTran;
	}
	public String getNomAgencia() {
		return nomAgencia;
	}
	public Integer getHoraSistema() {
		return horaSistema;
	}
	public String getNumIdentificacionCliente() {
		return numIdentificacionCliente;
	}
	public String getPeriodoPago() {
		return periodoPago;
	}
	public String getDescTipoDocumento() {
		return descTipoDocumento;
	}
	public String getImpresionFISDL() {
		return impresionFISDL;
	}
	public String getDui() {
		return dui;
	}
	public String getNomCliente() {
		return nomCliente;
	}
	public String getConvenio() {
		return convenio;
	}
	public String getTipoDocumento() {
		return tipoDocumento;
	}
	public String getCodPlanilla() {
		return codPlanilla;
	}
	public String getProgramaFISDL() {
		return programaFISDL;
	}
	public void setFechaTransaccion(Integer fechaTransaccion) {
		this.fechaTransaccion = fechaTransaccion;
	}
	public void setNumTran(String numTran) {
		this.numTran = numTran;
	}
	public void setNomAgencia(String nomAgencia) {
		this.nomAgencia = nomAgencia;
	}
	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}
	public void setNumIdentificacionCliente(String numIdentificacionCliente) {
		this.numIdentificacionCliente = numIdentificacionCliente;
	}
	public void setPeriodoPago(String periodoPago) {
		this.periodoPago = periodoPago;
	}
	public void setDescTipoDocumento(String descTipoDocumento) {
		this.descTipoDocumento = descTipoDocumento;
	}
	public void setImpresionFISDL(String impresionFISDL) {
		this.impresionFISDL = impresionFISDL;
	}
	public void setDui(String dui) {
		this.dui = dui;
	}
	public void setNomCliente(String nomCliente) {
		this.nomCliente = nomCliente;
	}
	public void setConvenio(String convenio) {
		this.convenio = convenio;
	}
	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	public void setCodPlanilla(String codPlanilla) {
		this.codPlanilla = codPlanilla;
	}
	public void setProgramaFISDL(String programaFISDL) {
		this.programaFISDL = programaFISDL;
	}
	public Integer getCodColector() {
		return codColector;
	}
	public void setCodColector(Integer codColector) {
		this.codColector = codColector;
	}
	public Integer getCodTipoDocumento() {
		return codTipoDocumento;
	}
	public void setCodTipoDocumento(Integer codTipoDocumento) {
		this.codTipoDocumento = codTipoDocumento;
	}
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
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
	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}
	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}
	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
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
	public Integer getCodSubCausal() {
		return codSubCausal;
	}
	public void setCodSubCausal(Integer codSubCausal) {
		this.codSubCausal = codSubCausal;
	}
	public Integer getCodCausal() {
		return codCausal;
	}
	public void setCodCausal(Integer codCausal) {
		this.codCausal = codCausal;
	}
	public Long getGlbDtime() {
		return glbDtime;
	}
	public void setGlbDtime(Long glbDtime) {
		this.glbDtime = glbDtime;
	}
	public String getCamposFormulario() {
		return camposFormulario;
	}
	public void setCamposFormulario(String camposFormulario) {
		this.camposFormulario = camposFormulario;
	}
	@Override
	public String toString() {
		return "PagaduriaGenericoRespuesta [fechaTransaccion=" + fechaTransaccion + ", numTran=" + numTran
				+ ", nomAgencia=" + nomAgencia + ", horaSistema=" + horaSistema + ", numIdentificacionCliente="
				+ numIdentificacionCliente + ", periodoPago=" + periodoPago + ", descTipoDocumento=" + descTipoDocumento
				+ ", impresionFISDL=" + impresionFISDL + ", dui=" + dui + ", nomCliente=" + nomCliente + ", convenio="
				+ convenio + ", tipoDocumento=" + tipoDocumento + ", codPlanilla=" + codPlanilla + ", programaFISDL="
				+ programaFISDL + ", codColector=" + codColector + ", codTipoDocumento=" + codTipoDocumento
				+ ", codOficinaTran=" + codOficinaTran + ", codTran=" + codTran + ", numDocumentoTran="
				+ numDocumentoTran + ", valorMovimiento=" + valorMovimiento + ", camposFormulario=" + camposFormulario
				+ ", valorEfectivo=" + valorEfectivo + ", codTerminal=" + codTerminal + ", codCajero=" + codCajero
				+ ", nomCajero=" + nomCajero + ", codPantalla=" + codPantalla + ", codCausal=" + codCausal
				+ ", codSubCausal=" + codSubCausal + ", glbDtime=" + glbDtime + "]";
	}

}
