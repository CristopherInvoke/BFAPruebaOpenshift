package sv.gob.bfa.deposito.mixto.cuentaahorro.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;

public class ReversaDepositoMixtoCuentaAhorroPeticion extends Peticion{
	
	
	private static final long serialVersionUID = 1L;
	private Integer 		codTran;
	private Integer			numDocumentoTran;
	private Integer 		numReversa;
	private String 			cuentaAhorro;
	private Integer 		numLibreta;
	private Integer 		tipDocumentoPerSimplifica; 
	private String 			numDocumentoPerSimplifica;
	private Integer 		tipDocumentoPersona;
	private String 			numDocumentoPersona;
	private String 			nombrePersona;
	private Integer 		numTransLavado;
	private BigDecimal 		valorEfectivo;
	private BigDecimal 		valorCheques;
	private BigDecimal		valorChequesPropios;
	private BigDecimal		valorChequesAjenos;
	private BigDecimal 		valorChequesExt;
	private BigDecimal 		valorMovimiento;
	private Integer 		codOficinaTran;
	private Integer 		codTerminal; 
	private String 			codCajero; 
	private Integer 		numCaja; 
	private Integer 		senSupervisor;
	private Integer 		senValidacionSaldos;
	private List<Cheque> 	cheques;
	

	public Integer getCodTran() {
		return codTran;
	}

	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}

	public String getCuentaAhorro() {
		return cuentaAhorro;
	}

	public void setCuentaAhorro(String cuentaAhorro) {
		this.cuentaAhorro = cuentaAhorro;
	}

	public Integer getNumLibreta() {
		return numLibreta;
	}

	public void setNumLibreta(Integer numLibreta) {
		this.numLibreta = numLibreta;
	}

	public Integer getTipDocumentoPerSimplifica() {
		return tipDocumentoPerSimplifica;
	}

	public void setTipDocumentoPerSimplifica(Integer tipDocumentoPerSimplifica) {
		this.tipDocumentoPerSimplifica = tipDocumentoPerSimplifica;
	}

	public String getNumDocumentoPerSimplifica() {
		return numDocumentoPerSimplifica;
	}

	public void setNumDocumentoPerSimplifica(String numDocumentoPerSimplifica) {
		this.numDocumentoPerSimplifica = numDocumentoPerSimplifica;
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

	public BigDecimal getValorChequesExt() {
		return valorChequesExt;
	}

	public void setValorChequesExt(BigDecimal valorChequesExt) {
		this.valorChequesExt = valorChequesExt;
	}

	public BigDecimal getValorMovimiento() {
		return valorMovimiento;
	}

	public void setValorMovimiento(BigDecimal valorMovimiento) {
		this.valorMovimiento = valorMovimiento;
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

	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
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

	public Integer getSenSupervisor() {
		return senSupervisor;
	}

	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}

	public Integer getSenValidacionSaldos() {
		return senValidacionSaldos;
	}

	public void setSenValidacionSaldos(Integer senValidacionSaldos) {
		this.senValidacionSaldos = senValidacionSaldos;
	}

	@Override
	public String toString() {
		return "ReversaDepositoMixtoCuentaAhorroPeticion "
				+ "[codTran=" 						+ codTran 
				+ ", numDocumentoTran=" 			+ numDocumentoTran
				+ ", numReversa=" 					+ numReversa 
				+ ", cuentaAhorro=" 				+ cuentaAhorro 
				+ ", numLibreta=" 					+ numLibreta
				+ ", tipDocumentoPerSimplifica=" 	+ tipDocumentoPerSimplifica 
				+ ", numDocumentoPerSimplifica="	+ numDocumentoPerSimplifica 
				+ ", tipDocumentoPersona=" 			+ tipDocumentoPersona 
				+ ", numDocumentoPersona="			+ numDocumentoPersona 
				+ ", nombrePersona=" 				+ nombrePersona 
				+ ", numTransLavado=" 				+ numTransLavado
				+ ", valorEfectivo=" 				+ valorEfectivo 
				+ ", valorCheques=" 				+ valorCheques 
				+ ", valorChequesPropios="			+ valorChequesPropios 
				+ ", valorChequesAjenos=" 			+ valorChequesAjenos 
				+ ", valorChequesExt="				+ valorChequesExt 
				+ ", valorMovimiento=" 				+ valorMovimiento 
				+ ", codOficinaTran=" 				+ codOficinaTran
				+ ", codTerminal=" 					+ codTerminal 
				+ ", codCajero=" 					+ codCajero 
				+ ", numCaja=" 						+ numCaja
				+ ", senSupervisor=" 				+ senSupervisor 
				+ ", senValidacionSaldos=" 			+ senValidacionSaldos 
				+ ", cheques="						+ cheques 
				+ "]";
	}
	
}
