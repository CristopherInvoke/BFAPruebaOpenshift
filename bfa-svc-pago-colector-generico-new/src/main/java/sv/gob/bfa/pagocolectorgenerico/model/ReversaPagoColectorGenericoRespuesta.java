package sv.gob.bfa.pagocolectorgenerico.model;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Respuesta;

@XmlRootElement(name="ReversaPagoColectorGenericoRespuesta")
public class ReversaPagoColectorGenericoRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	private Integer fechaTransaccion;
	private String nomAgencia;
	private Integer horaSistema;
	private String numIdentificacionCliente;
	private String numCredito;
	private String periodoPago;
	private String descTipoDocumento;
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
	public String getNumCredito() {
		return numCredito;
	}
	public void setNumCredito(String numCredito) {
		this.numCredito = numCredito;
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
	public static long getSerialversionuid() {
		return serialVersionUID;
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
		return "ReversaPagoColectorGenericoRespuesta [fechaTransaccion=" + fechaTransaccion + ", nomAgencia="
				+ nomAgencia + ", horaSistema=" + horaSistema + ", numIdentificacionCliente=" + numIdentificacionCliente
				+ ", numCredito=" + numCredito + ", periodoPago=" + periodoPago + ", descTipoDocumento="
				+ descTipoDocumento + ", codCausal=" + codCausal + ", codSubCausal=" + codSubCausal + "]";
	}
	
}
