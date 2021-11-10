package sv.gob.bfa.deposito.mixto.cuentaahorro.model;

import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;

public class DepositoMixtoCuentaAhorroPeticion extends Peticion{
	
	
	
	private static final long serialVersionUID = 1L;

	
	private Integer 		codTran;
	private Integer 		numDocumentoTran;
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
	private BigDecimal 		valorChequesPropios;
	private BigDecimal 		valorChequesAjenos;
	private BigDecimal 		valorChequesExt;
	private BigDecimal 		valorMovimiento;
	private Integer 		numLineaPosteo;
	private BigDecimal 		saldoLibreta;
	private Integer 		libretaValida; 
	private Integer 		codOficinaTran;
	private Integer 		codTerminal; 	
	private String 			codCajero; 
	private Integer 		numCaja; 
	private Integer 		senSupervisor;
	private Integer 		senValidacionSaldos;
	private String 			numTarjeta;
	private List<Cheque> 	cheques;
	

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

	public Integer getNumLineaPosteo() {
		return numLineaPosteo;
	}

	public void setNumLineaPosteo(Integer numLineaPosteo) {
		this.numLineaPosteo = numLineaPosteo;
	}

	public BigDecimal getSaldoLibreta() {
		return saldoLibreta;
	}

	public void setSaldoLibreta(BigDecimal saldoLibreta) {
		this.saldoLibreta = saldoLibreta;
	}

	public Integer getLibretaValida() {
		return libretaValida;
	}

	public void setLibretaValida(Integer libretaValida) {
		this.libretaValida = libretaValida;
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

	public Integer getSenSupervisor() {
		return senSupervisor;
	}

	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}

	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}

	public Integer getSenValidacionSaldos() {
		return senValidacionSaldos;
	}

	public void setSenValidacionSaldos(Integer senValidacionSaldos) {
		this.senValidacionSaldos = senValidacionSaldos;
	}
	
	public String getNumTarjeta() {
		return numTarjeta;
	}

	public void setNumTarjeta(String numTarjeta) {
		this.numTarjeta = numTarjeta;
	}

	@Override
	public String toString() {
		return "DepositoMixtoCuentaAhorroPeticion "
				+ "["
				+ "  codTran=" 						+ codTran 
				+ ", numDocumentoTran=" 			+ numDocumentoTran
				+ ", cuentaAhorro=" 				+ cuentaAhorro 
				+ ", numLibreta=" 					+ numLibreta 
				+ ", tipDocumentoPerSimplifica="	+ tipDocumentoPerSimplifica 
				+ ", numDocumentoPerSimplifica=" 	+ numDocumentoPerSimplifica
				+ ", tipDocumentoPersona=" 			+ tipDocumentoPersona 
				+ ", numDocumentoPersona=" 			+ numDocumentoPersona
				+ ", nombrePersona=" 				+ nombrePersona 
				+ ", numTransLavado=" 				+ numTransLavado 
				+ ", valorEfectivo="				+ valorEfectivo 
				+ ", valorCheques=" 				+ valorCheques 
				+ ", valorChequesPropios=" 			+ valorChequesPropios
				+ ", valorChequesAjenos=" 			+ valorChequesAjenos 
				+ ", valorChequesExt=" 				+ valorChequesExt
				+ ", valorMovimiento=" 				+ valorMovimiento 
				+ ", numLineaPosteo=" 				+ numLineaPosteo 
				+ ", saldoLibreta="					+ saldoLibreta 
				+ ", libretaValida=" 				+ libretaValida 
				+ ", codOficinaTran=" 				+ codOficinaTran
				+ ", codTerminal=" 					+ codTerminal 
				+ ", codCajero=" 					+ codCajero 
				+ ", numCaja=" 						+ numCaja
				+ ", senSupervisor=" 				+ senSupervisor 
				+ ", senValidacionSaldos=" 			+ senValidacionSaldos 
				+ ", numTarjeta="					+ numTarjeta 
				+ ", cheques=" + cheques 
				+ "]";
	}
	
}
