package sv.gob.bfa.pagoprestamo.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;

public class ReversaPagoPrestamoPeticion extends Peticion{

	private static final long serialVersionUID = 1L;
	
	private Integer codTran;
	private Integer numReversa;
	private Integer numDocumentoTran;
	private String cuentaPrestamo;
	private Integer senSupervisor;
	private Integer codTerminal;
	private BigDecimal valorMovimiento;
	private Integer tipDocumentoPersona;
	private String numDocumentoPersona;
	private String nombrePersona;
	private Integer numTransLavado;
	private BigDecimal valorEfectivo;
	private BigDecimal valorCheques;
	private BigDecimal valorChequesPropios;
	private BigDecimal valorChequesAjenos;
	private Integer codOficinaTran;
	private String codCajero;
	private Integer numCaja;
	private List<Cheque> cheques;
	
	public Integer getCodTran() {
		return codTran;
	}



	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}



	public Integer getNumReversa() {
		return numReversa;
	}



	public void setNumReversa(Integer numReversa) {
		this.numReversa = numReversa;
	}



	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}



	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}



	public String getCuentaPrestamo() {
		return cuentaPrestamo;
	}



	public void setCuentaPrestamo(String cuentaPrestamo) {
		this.cuentaPrestamo = cuentaPrestamo;
	}



	public Integer getSenSupervisor() {
		return senSupervisor;
	}



	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}



	public Integer getCodTerminal() {
		return codTerminal;
	}



	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}



	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}



	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
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



	public Integer getNumTransLavado() {
		return numTransLavado;
	}



	public void setNumTransLavado(Integer numTransLavado) {
		this.numTransLavado = numTransLavado;
	}



	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}



	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}



	public BigDecimal getValorCheques() {
		return valorCheques;
	}



	public void setValorCheques(BigDecimal valorCheques) {
		this.valorCheques = valorCheques;
	}



	public BigDecimal getValorChequesPropios() {
		return valorChequesPropios;
	}



	public void setValorChequesPropios(BigDecimal valorChequesPropios) {
		this.valorChequesPropios = valorChequesPropios;
	}



	public BigDecimal getValorChequesAjenos() {
		return valorChequesAjenos;
	}



	public void setValorChequesAjenos(BigDecimal valorChequesAjenos) {
		this.valorChequesAjenos = valorChequesAjenos;
	}



	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}



	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
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

	public List<Cheque> getCheques() {
		return cheques;
	}



	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}



	@Override
	public String toString() {
		return "PagoPrestamoReversaPeticion [codTran=" + codTran + ", numReversa=" + numReversa + ", numDocumentoTran="
				+ numDocumentoTran + ", cuentaPrestamo=" + cuentaPrestamo + ", senSupervisor=" + senSupervisor
				+ ", codTerminal=" + codTerminal + ", valorMovimiento=" + valorMovimiento + ", tipDocumentoPersona="
				+ tipDocumentoPersona + ", numDocumentoPersona=" + numDocumentoPersona + ", nombrePersona="
				+ nombrePersona + ", numTransLavado=" + numTransLavado + ", valorEfectivo=" + valorEfectivo
				+ ", valorCheques=" + valorCheques + ", valorChequesPropios=" + valorChequesPropios
				+ ", valorChequesAjenos=" + valorChequesAjenos + ", codOficinaTran=" + codOficinaTran + ", codCajero="
				+ codCajero + ", numCaja=" + numCaja + ", cheques=" + cheques + "]";
	}
	
	

}
