package sv.gob.bfa.conectores.servicio.delsur.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

import sv.gob.bfa.soporte.comunes.dto.Respuesta;

@XmlRootElement(name = "ConectoresServiciosDelsurRespuesta")
@ApiModel(description = "ConectoresServiciosDelsurRespuesta")
public class ConectoresServiciosDelsurRespuesta extends Respuesta implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String referenciaDELSUR;
	private String nis;
	private String nombreCliente;
	private BigDecimal montoEnergia;
	private BigDecimal montoAlcaldia;
	private BigDecimal montoReconexion;
	
	public String getReferenciaDELSUR() {
		return referenciaDELSUR;
	}
	public void setReferenciaDELSUR(String referenciaDELSUR) {
		this.referenciaDELSUR = referenciaDELSUR;
	}
	public String getNis() {
		return nis;
	}
	public void setNis(String nis) {
		this.nis = nis;
	}
	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public BigDecimal getMontoEnergia() {
		return montoEnergia;
	}
	public void setMontoEnergia(BigDecimal montoEnergia) {
		this.montoEnergia = montoEnergia;
	}
	public BigDecimal getMontoAlcaldia() {
		return montoAlcaldia;
	}
	public void setMontoAlcaldia(BigDecimal montoAlcaldia) {
		this.montoAlcaldia = montoAlcaldia;
	}
	public BigDecimal getMontoReconexion() {
		return montoReconexion;
	}
	public void setMontoReconexion(BigDecimal montoReconexion) {
		this.montoReconexion = montoReconexion;
	}
	
	
	
}
