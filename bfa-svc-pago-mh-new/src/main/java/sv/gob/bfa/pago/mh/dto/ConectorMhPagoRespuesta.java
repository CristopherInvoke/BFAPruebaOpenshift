package sv.gob.bfa.pago.mh.dto;


import javax.xml.bind.annotation.XmlRootElement;


//import com.wordnik.swagger.annotations.ApiModel;

import sv.gob.bfa.core.model.Respuesta;
//import sv.gob.bfa.soporte.comunes.dto.Respuesta;

@XmlRootElement(name = "ConectorMhPagoRespuesta")
//@XmlRootElement(name = "ConectoresServiciosMhRespuesta")
public class ConectorMhPagoRespuesta extends Respuesta{

	private static final long serialVersionUID = 1L;
	
	
	private String codMH;
	private String descMH;
	
	public String getCodMH() {
		return codMH;
	}
	public void setCodMH(String codMH) {
		this.codMH = codMH;
	}
	public String getDescMH() {
		return descMH;
	}
	public void setDescMH(String descMH) {
		this.descMH = descMH;
	}
	@Override
	public String toString() {
		return "ConectorMhPagoRespuesta [codMH=" + codMH + ", descMH=" + descMH + "]";
	}
	
}