package sv.gob.bfa.pagaduriagenerico.model;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Peticion;

@XmlRootElement(name="PagaduriaGenericoPeticion")
public class PagaduriaGenericoPeticion extends Peticion{
	
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

	private Integer numCaja;
	
	private Integer numLote;
	
	private BigDecimal valorEfectivo;
	
	private BigDecimal valorCheques;
	
	private Long glbDtime;

	public Integer getNumLote() {
		return numLote;
	}

	public void setNumLote(Integer numLote) {
		this.numLote = numLote;
	}

	public Integer getCodColector() {
		return codColector;
	}

	public Integer getCodTipoDocumento() {
		return codTipoDocumento;
	}

	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}

	public Integer getCodTran() {
		return codTran;
	}

	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}

	public Integer getSenSupervisor() {
		return senSupervisor;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public String getCamposFormulario() {
		return camposFormulario;
	}

	public String getCodCajero() {
		return codCajero;
	}

	public Integer getNumCaja() {
		return numCaja;
	}

	public void setCodColector(Integer codColector) {
		this.codColector = codColector;
	}

	public void setCodTipoDocumento(Integer codTipoDocumento) {
		this.codTipoDocumento = codTipoDocumento;
	}

	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}

	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}

	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}

	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
	}

	public void setCamposFormulario(String camposFormulario) {
		this.camposFormulario = camposFormulario;
	}

	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}

	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}
	
	public Integer getCodTerminal() {
		return codTerminal;
	}

	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
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

	public Long getGlbDtime() {
		return glbDtime;
	}

	public void setGlbDtime(Long glbDtime) {
		this.glbDtime = glbDtime;
	}

	@Override
	public String toString() {
		return "PagaduriaGenericoPeticion [codColector=" + codColector + ", codTipoDocumento=" + codTipoDocumento
				+ ", codOficinaTran=" + codOficinaTran + ", codTran=" + codTran + ", numDocumentoTran="
				+ numDocumentoTran + ", senSupervisor=" + senSupervisor + ", valorMovimiento=" + valorMovimiento
				+ ", camposFormulario=" + camposFormulario + ", codTerminal=" + codTerminal + ", codCajero=" + codCajero
				+ ", numCaja=" + numCaja + ", numLote=" + numLote + ", valorEfectivo=" + valorEfectivo
				+ ", valorCheques=" + valorCheques + ", glbDtime=" + glbDtime + "]";
	}

}
