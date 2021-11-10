package sv.gob.bfa.pagaduriagenerico.model;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Respuesta;

@XmlRootElement(name="ReversaPagaduriaGenericoRespuesta")
public class ReversaPagaduriaGenericoRespuesta extends Respuesta{
	
	private static final long serialVersionUID = 1L;
	
	private Integer fechaTransaccion;
	private String nomAgencia;
	private Integer horaSistema;
	private String numIdentificacionCliente;
	private String periodoPago;
	private String descTipoDocumento;
	private String dui;
	private String nomCliente;
	private String convenio;
	private String tipoDocumento;
	private String codPlanilla;
	private String programaFISDL;
	private Integer codCausal;
	private Integer codSubCausal;
	public Integer getFechaTransaccion() {
		return fechaTransaccion;
	}
	public void setFechaTransaccion(Integer fechaTransaccion) {
		this.fechaTransaccion = fechaTransaccion;
	}
	public String getNomAgencia() {
		return nomAgencia;
	}
	public void setNomAgencia(String nomAgencia) {
		this.nomAgencia = nomAgencia;
	}
	public Integer getHoraSistema() {
		return horaSistema;
	}
	public void setHoraSistema(Integer horaSistema) {
		this.horaSistema = horaSistema;
	}
	public String getNumIdentificacionCliente() {
		return numIdentificacionCliente;
	}
	public void setNumIdentificacionCliente(String numIdentificacionCliente) {
		this.numIdentificacionCliente = numIdentificacionCliente;
	}
	public String getPeriodoPago() {
		return periodoPago;
	}
	public void setPeriodoPago(String periodoPago) {
		this.periodoPago = periodoPago;
	}
	public String getDescTipoDocumento() {
		return descTipoDocumento;
	}
	public void setDescTipoDocumento(String descTipoDocumento) {
		this.descTipoDocumento = descTipoDocumento;
	}
	public String getDui() {
		return dui;
	}
	public void setDui(String dui) {
		this.dui = dui;
	}
	public String getNomCliente() {
		return nomCliente;
	}
	public void setNomCliente(String nomCliente) {
		this.nomCliente = nomCliente;
	}
	public String getConvenio() {
		return convenio;
	}
	public void setConvenio(String convenio) {
		this.convenio = convenio;
	}
	public String getTipoDocumento() {
		return tipoDocumento;
	}
	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	public String getCodPlanilla() {
		return codPlanilla;
	}
	public void setCodPlanilla(String codPlanilla) {
		this.codPlanilla = codPlanilla;
	}
	public String getProgramaFISDL() {
		return programaFISDL;
	}
	public void setProgramaFISDL(String programaFISDL) {
		this.programaFISDL = programaFISDL;
	}
	public Integer getCodCausal() {
		return codCausal;
	}
	public void setCodCausal(Integer codCausal) {
		this.codCausal = codCausal;
	}
	public Integer getCodSubCausal() {
		return codSubCausal;
	}
	public void setCodSubCausal(Integer codSubCausal) {
		this.codSubCausal = codSubCausal;
	}
	@Override
	public String toString() {
		return "ReversaPagaduriaGenericoRespuesta [fechaTransaccion=" + fechaTransaccion + ", nomAgencia=" + nomAgencia
				+ ", horaSistema=" + horaSistema + ", numIdentificacionCliente=" + numIdentificacionCliente
				+ ", periodoPago=" + periodoPago + ", descTipoDocumento=" + descTipoDocumento + ", dui=" + dui
				+ ", nomCliente=" + nomCliente + ", convenio=" + convenio + ", tipoDocumento=" + tipoDocumento
				+ ", codPlanilla=" + codPlanilla + ", programaFISDL=" + programaFISDL
				+ ", codCausal=" + codCausal + ", codSubCausal=" + codSubCausal + "]";
	}
	
}
