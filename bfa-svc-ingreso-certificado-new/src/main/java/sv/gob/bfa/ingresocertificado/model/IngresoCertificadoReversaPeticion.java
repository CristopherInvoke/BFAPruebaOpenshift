package sv.gob.bfa.ingresocertificado.model;
import java.math.BigDecimal;
import java.util.List;

import sv.gob.bfa.core.model.Cheque;
import sv.gob.bfa.core.model.Peticion;

public class IngresoCertificadoReversaPeticion extends Peticion {

	private static final long serialVersionUID = -4011505335432250743L;
	
	private Integer 	 codProductoAux;
	private Integer 	 numDocumentoTran;
	private BigDecimal	 valorEfectivo;
	private BigDecimal 	 valorCheques;
	private BigDecimal 	 valorChequesPropios;
	private BigDecimal 	 valorChequesAjenos;
	private BigDecimal 	 valorChequesExt;
	private BigDecimal 	 valorMovimiento;
	private Integer 	 codTran;
	private Integer 	 senSupervisor;
	private Integer 	 numCaja;
	private String 		 codCajero;
	private Integer 	 codTerminal;
	private Integer 	 codOficinaTran;
	private Integer 	 numReversa;
	private List<Cheque> cheques;

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

	public Integer getNumReversa() {
		return numReversa;
	}

	public void setNumReversa(Integer numReversa) {
		this.numReversa = numReversa;
	}
	
	public List<Cheque> getCheques() {
		return cheques;
	}


	public void setCheques(List<Cheque> cheques) {
		this.cheques = cheques;
	}

	@Override
	public String toString() {
		return "IngresoCertificadoReversaPeticion "
				+ "["
				+ "  codProductoAux=" 		+ codProductoAux 
				+ ", numDocumentoTran="		+ numDocumentoTran 
				+ ", valorEfectivo=" 		+ valorEfectivo 
				+ ", valorCheques=" 		+ valorCheques
				+ ", valorChequesPropios="  + valorChequesPropios
				+ ", valorChequesAjenos="   + valorChequesAjenos 
				+ ", valorChequesExt="      + valorChequesExt
				+ ", valorMovimiento=" 		+ valorMovimiento 
				+ ", codTran=" 				+ codTran 
				+ ", senSupervisor=" 		+ senSupervisor
				+ ", numCaja=" 				+ numCaja 
				+ ", codCajero="			+ codCajero 
				+ ", codTerminal=" 			+ codTerminal
				+ ", codOficinaTran=" 		+ codOficinaTran 
				+ ", numReversa=" 			+ numReversa
				+ ", cheques=" 				+ cheques
				+ "]";
	}

}
