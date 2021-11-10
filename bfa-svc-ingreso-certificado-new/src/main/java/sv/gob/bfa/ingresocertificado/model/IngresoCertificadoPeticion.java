package sv.gob.bfa.ingresocertificado.model;
import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;

/**
* Clase correspondiente a la petición esperada por el servicio
* Ingreso de Certificado. Contiene todos los parámetros que el servicio espera.
*/

public class IngresoCertificadoPeticion extends Peticion {

	private static final long serialVersionUID = 7623975906043624832L;
	
	private Integer   		codProductoAux;
	private Integer   		numDocumentoTran;
	private BigDecimal		valorEfectivo;
	private BigDecimal 		valorCheques;
	private BigDecimal  	valorChequesPropios;
	private BigDecimal  	valorChequesAjenos;
	private BigDecimal 		valorChequesExt;
	private BigDecimal 		valorMovimiento;
	private Integer    		tipDocumentoPersona;
	private String     		numDocumentoPersona;
	private String     		nombrePersona;
	private Integer    		numTransLavado;
	private Integer    		codTran;
	private Integer    		senSupervisor;
	private Integer    		numCaja;
	private String     		codCajero;
	private Integer    		codTerminal;
	private Integer    		codOficinaTran;
	private List<Cheque> 	cheques;
	
	public Integer getCodProductoAux() {
		return codProductoAux;
	}
	
	public void setCodProductoAux(Integer codProductoAux) {
		this.codProductoAux = codProductoAux;
	}
	
	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}
	
	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
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
	
	public Integer getCodTran() {
		return codTran;
	}
	
	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}
	
	public Integer getSenSupervisor() {
		return senSupervisor;
	}
	
	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}
	
	public Integer getNumCaja() {
		return numCaja;
	}
	
	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}
	
	public String getCodCajero() {
		return codCajero;
	}
	
	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}
	
	public Integer getCodTerminal() {
		return codTerminal;
	}
	
	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}
	
	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}
	
	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}
	
	public List<Cheque> getCheques() {
		return cheques;
	}

	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}
	
	@Override
	public String toString() {
		return "IngresoCertificadoPeticion "
				+ " ["
				+ "  numDocumentoTran=" 	+ numDocumentoTran 
				+ ", valorEfectivo="		+ valorEfectivo 
				+ ", valorCheques=" 		+ valorCheques
				+ ", valorChequesPropios="  + valorChequesPropios
				+ ", valorChequesAjenos="   + valorChequesAjenos 
				+ ", valorChequesExt="      + valorChequesExt
				+ ", valorMovimiento="		+ valorMovimiento
				+ ", tipDocumentoPersona=" 	+ tipDocumentoPersona 
				+ ", numDocumentoPersona=" 	+ numDocumentoPersona
				+ ", nombrePersona=" 		+ nombrePersona 
				+ ", numTransLavado=" 		+ numTransLavado 
				+ ", codTran=" 				+ codTran
				+ ", senSupervisor=" 		+ senSupervisor 
				+ ", numCaja=" 				+ numCaja 
				+ ", codCajero=" 			+ codCajero
				+ ", codTerminal=" 			+ codTerminal 
				+ ", codOficinaTran=" 		+ codOficinaTran
				+ ", cheques=" 				+ cheques
				+ "]";
	}
}
