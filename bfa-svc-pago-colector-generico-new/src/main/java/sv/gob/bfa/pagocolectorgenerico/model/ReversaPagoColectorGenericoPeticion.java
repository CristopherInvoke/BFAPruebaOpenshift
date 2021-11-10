package sv.gob.bfa.pagocolectorgenerico.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ReversaPagoColectorGenericoPeticion")
public class ReversaPagoColectorGenericoPeticion {

	private Integer codColector;
	private Integer codTipoDocumento;
	private Integer codOficinaTran;
	private Integer codTran;
	private Integer numDocumentoTran;
	private BigDecimal valorMovimiento;
	private String camposFormulario;
	private Integer codTerminal;
	private String codCajero;
	private Integer numReversa;
	
	public String getCodCajero() {
		return codCajero;
	}
	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
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
	public Integer getCodTipoDocumento() {
		return codTipoDocumento;
	}
	public void setCodTipoDocumento(Integer codTipoDocumento) {
		this.codTipoDocumento = codTipoDocumento;
	}
	public Integer getCodColector() {
		return codColector;
	}
	public void setCodColector(Integer codColector) {
		this.codColector = codColector;
	}
	public String getCamposFormulario() {
		return camposFormulario;
	}
	public void setCamposFormulario(String camposFormulario) {
		this.camposFormulario = camposFormulario;
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
	public Integer getCodTerminal() {
		return codTerminal;
	}
	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}
	public Integer getNumReversa() {
		return numReversa;
	}
	public void setNumReversa(Integer numReversa) {
		this.numReversa = numReversa;
	}
	@Override
	public String toString() {
		return "ReversaPagoColectorGenericoPeticion [codColector=" + codColector + ", codTipoDocumento="
				+ codTipoDocumento + ", codOficinaTran=" + codOficinaTran + ", codTran=" + codTran
				+ ", numDocumentoTran=" + numDocumentoTran + ", valorMovimiento=" + valorMovimiento
				+ ", camposFormulario=" + camposFormulario + ", codTerminal=" + codTerminal + ", codCajero=" + codCajero
				+ ", numReversa=" + numReversa + "]";
	}
	
}
