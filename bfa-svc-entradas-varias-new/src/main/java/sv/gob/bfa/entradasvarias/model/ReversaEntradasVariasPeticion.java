package sv.gob.bfa.entradasvarias.model;

import java.math.BigDecimal;

import sv.gob.bfa.core.model.Peticion;

public class ReversaEntradasVariasPeticion extends Peticion{

	private static final long serialVersionUID = 1L;
	
	private Integer codTran;
	private Integer numDocumentoTran;
	private Integer codCausal;
	private String cuentaTransaccion;
	private BigDecimal valorEfectivo;
	private Integer tipDocumentoPersona;
	private String numDocumentoPersona;
	private String nombrePersona;
	private Integer codOficinaTran;
	private Integer codTerminal;
	private String codCajero;
	private Integer numCaja;
	private Integer numReversa;
	private Integer codSubcausal;
	
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
	public Integer getCodCausal() {
		return codCausal;
	}
	public void setCodCausal(Integer codCausal) {
		this.codCausal = codCausal;
	}
	public String getCuentaTransaccion() {
		return cuentaTransaccion;
	}
	public void setCuentaTransaccion(String cuentaTransaccion) {
		this.cuentaTransaccion = cuentaTransaccion;
	}
	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}
	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}
	public Integer getTipDocumentoPersona() {
		return tipDocumentoPersona;
	}
	public void setTipDocumentoPersona(Integer tipDocumentoPersona) {
		this.tipDocumentoPersona = tipDocumentoPersona;
	}
	public String getNumDocumentoPersona() {
		return numDocumentoPersona;
	}
	public void setNumDocumentoPersona(String numDocumentoPersona) {
		this.numDocumentoPersona = numDocumentoPersona;
	}
	
	public String getNombrePersona() {
		return nombrePersona;
	}
	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
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
	public Integer getNumCaja() {
		return numCaja;
	}
	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}
	public Integer getNumReversa() {
		return numReversa;
	}
	public void setNumReversa(Integer numReversa) {
		this.numReversa = numReversa;
	}
	public Integer getCodSubcausal() {
		return codSubcausal;
	}
	public void setCodSubcausal(Integer codSubcausal) {
		this.codSubcausal = codSubcausal;
	}
	@Override
	public String toString() {
		return "ReversaEntradasVariasPeticion [codTran=" + codTran + ", numDocumentoTran=" + numDocumentoTran
				+ ", codCausal=" + codCausal + ", cuentaTransaccion=" + cuentaTransaccion + ", valorEfectivo="
				+ numDocumentoPersona + ", nombrePersona=" + nombrePersona + ", codOficinaTran=" + codOficinaTran
				+ ", codTerminal=" + codTerminal + ", codCajero=" + codCajero + ", numCaja=" + numCaja + ", numReversa="
				+ numReversa + ", codSubcausal=" + codSubcausal + "]";
	}
	
}
