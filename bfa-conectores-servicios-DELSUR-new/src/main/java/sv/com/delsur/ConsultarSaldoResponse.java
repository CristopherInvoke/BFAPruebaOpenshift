
package sv.com.delsur;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Consultar_SaldoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "consultarSaldoResult"
})
@XmlRootElement(name = "Consultar_SaldoResponse")
public class ConsultarSaldoResponse {

    @XmlElement(name = "Consultar_SaldoResult")
    protected String consultarSaldoResult;

    /**
     * Gets the value of the consultarSaldoResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsultarSaldoResult() {
        return consultarSaldoResult;
    }

    /**
     * Sets the value of the consultarSaldoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsultarSaldoResult(String value) {
        this.consultarSaldoResult = value;
    }

}
