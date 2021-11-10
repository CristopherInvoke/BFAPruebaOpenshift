package sv.gob.bfa.pagocolectorgenerico.model;

import sv.gob.bfa.core.model.Peticion;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PagoColectorGenericoPeticion")
public class PagoColectorGenericoPeticion extends Peticion{
	
	private static final long serialVersionUID = 1L;
	
	private Integer codColector;
	
	private Integer codTipoDocumento;
	
	private Integer codOficinaTran;
	
	private Integer codTran;
	
	private Integer numDocumentoTran;

	private Integer senSupervisor;
	
	private BigDecimal valorMovimiento;
	
	private String camposFormulario;
	
	private Integer codTerminal;
	
	private String codCajero;
	
	private Integer numLote;
	
	private Integer numCaja;
	
	private BigDecimal valorEfectivo;
	
	private BigDecimal valorCheques;

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

	public Integer getSenSupervisor() {
		return senSupervisor;
	}

	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}
	
	public String getCamposFormulario() {
		return camposFormulario;
	}

	public void setCamposFormulario(String camposFormulario) {
		this.camposFormulario = camposFormulario;
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

	public Integer getNumLote() {
		return numLote;
	}

	public void setNumLote(Integer numLote) {
		this.numLote = numLote;
	}

	public Integer getNumCaja() {
		return numCaja;
	}

	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}

	public BigDecimal getValorCheques() {
		return valorCheques;
	}

	public void setValorCheques(BigDecimal valorCheques) {
		this.valorCheques = valorCheques;
	}
	
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}

	@Override
	public String toString() {
		return "PagoColectorGenericoPeticion [codColector=" + codColector + ", codTipoDocumento=" + codTipoDocumento
				+ ", codOficinaTran=" + codOficinaTran + ", codTran=" + codTran + ", numDocumentoTran="
				+ numDocumentoTran + ", senSupervisor=" + senSupervisor + ", valorMovimiento=" + valorMovimiento
				+ ", camposFormulario=" + camposFormulario + ", codTerminal=" + codTerminal + ", codCajero=" + codCajero
				+ ", numLote=" + numLote + ", numCaja=" + numCaja + ", valorEfectivo=" + valorEfectivo
				+ ", valorCheques=" + valorCheques + "]";
	}

}
