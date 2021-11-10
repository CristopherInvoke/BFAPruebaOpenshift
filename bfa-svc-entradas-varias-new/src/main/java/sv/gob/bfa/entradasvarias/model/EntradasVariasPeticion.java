package sv.gob.bfa.entradasvarias.model;

import java.math.BigDecimal;

import sv.gob.bfa.core.model.Peticion;

public class EntradasVariasPeticion extends Peticion{
	
	private static final long serialVersionUID = 1L;
	
	private Integer codTran;

	private Integer senSupervisor;
	
	private Integer numDocumentoTran;
	
	private Integer codMoneda;
	
	private Integer codCausal;
	
	private String cuentaTransaccion;
	
	private BigDecimal valorEfectivo;
	
	private Integer numTransLavado;
	
	private Integer tipDocumentoPersona;
	
	private String numDocumentoPersona;
	
	private String nombrePersona;
	
	private Integer codOficinaTran;
	
	private Integer codTerminal;
	
	private String codCajero;
	
	private Integer numCaja;
	
	private Integer codSubcausal;
	

	public Integer getCodTran() {
		return codTran;
	}

	public Integer getSenSupervisor() {
		return senSupervisor;
	}

	public Integer getNumDocumentoTran() {
		return numDocumentoTran;
	}

	public Integer getCodMoneda() {
		return codMoneda;
	}

	public Integer getCodCausal() {
		return codCausal;
	}

	public String getCuentaTransaccion() {
		return cuentaTransaccion;
	}

	public BigDecimal getValorEfectivo() {
		return valorEfectivo;
	}

	public Integer getNumTransLavado() {
		return numTransLavado;
	}

	public Integer getTipDocumentoPersona() {
		return tipDocumentoPersona;
	}

	public String getNumDocumentoPersona() {
		return numDocumentoPersona;
	}

	public Integer getCodOficinaTran() {
		return codOficinaTran;
	}

	public Integer getCodTerminal() {
		return codTerminal;
	}

	public String getCodCajero() {
		return codCajero;
	}

	public Integer getNumCaja() {
		return numCaja;
	}

	public void setCodTran(Integer codTran) {
		this.codTran = codTran;
	}

	public void setSenSupervisor(Integer senSupervisor) {
		this.senSupervisor = senSupervisor;
	}

	public void setNumDocumentoTran(Integer numDocumentoTran) {
		this.numDocumentoTran = numDocumentoTran;
	}

	public void setCodMoneda(Integer codMoneda) {
		this.codMoneda = codMoneda;
	}

	public void setCodCausal(Integer codCausal) {
		this.codCausal = codCausal;
	}

	public void setCuentaTransaccion(String cuentaTransaccion) {
		this.cuentaTransaccion = cuentaTransaccion;
	}

	public void setValorEfectivo(BigDecimal valorEfectivo) {
		this.valorEfectivo = valorEfectivo;
	}

	public void setNumTransLavado(Integer numTransLavado) {
		this.numTransLavado = numTransLavado;
	}

	public void setTipDocumentoPersona(Integer tipDocumentoPersona) {
		this.tipDocumentoPersona = tipDocumentoPersona;
	}

	public void setNumDocumentoPersona(String numDocumentoPersona) {
		this.numDocumentoPersona = numDocumentoPersona;
	}

	public void setCodOficinaTran(Integer codOficinaTran) {
		this.codOficinaTran = codOficinaTran;
	}

	public void setCodTerminal(Integer codTerminal) {
		this.codTerminal = codTerminal;
	}

	public void setCodCajero(String codCajero) {
		this.codCajero = codCajero;
	}

	public void setNumCaja(Integer numCaja) {
		this.numCaja = numCaja;
	}

	public String getNombrePersona() {
		return nombrePersona;
	}

	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}

	public Integer getCodSubcausal() {
		return codSubcausal;
	}

	public void setCodSubcausal(Integer codSubcausal) {
		this.codSubcausal = codSubcausal;
	}
	
	
	
}
