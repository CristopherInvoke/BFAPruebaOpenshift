package sv.gob.bfa.pago.mh.model;


import javax.xml.bind.annotation.XmlRootElement;

import sv.gob.bfa.core.model.Peticion;

/**
 * Clase correspondiente a la petici&oacuten esperada por el servicio
 * Pago Ministerio de Hacienda. Contiene los par&aacutemetros que el servicio espera.
 */
@XmlRootElement(name="pagoMhPeticion")
public class PagoMhPeticion extends Peticion{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Número de pago electrónico
	 */
	private String codBarraNpe;
	
	/**
	 * codigo de causal
	 */
	private Integer codCausal;
	
	/**
	 * codigo de subcausal
	 */
	private Integer codSubCausal;
	
	/**
	 * Código de la transacci&oacuten
	 */
	private Integer codTran;
	
	/**
	 * Se&ntildeal de supervisión. Valores posibles son 1=Si , 2=No
	*/
	private Integer senSupervisor;


	public String getCodBarraNpe() {
		return codBarraNpe;
	}

	public void setCodBarraNpe(String codBarraNpe) {
		this.codBarraNpe = codBarraNpe;
	}

	public Integer getCodCausal() {
		return codCausal;
	}

	public void setCodCausal(Integer codCausal) {
		this.codCausal = codCausal;
	}

	public Integer getCodSubCausal() {
		return codSubCausal;
	}

	public void setCodSubCausal(Integer codSubCausal) {
		this.codSubCausal = codSubCausal;
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
	
}
