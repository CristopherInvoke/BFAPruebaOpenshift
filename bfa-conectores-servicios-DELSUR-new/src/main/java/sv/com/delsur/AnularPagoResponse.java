
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
 *         &lt;element name="Anular_PagoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
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
    "anularPagoResult"
})
@XmlRootElement(name = "Anular_PagoResponse")
public class AnularPagoResponse {

    @XmlElement(name = "Anular_PagoResult")
    protected String anularPagoResult;

    /**
     * Gets the value of the anularPagoResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnularPagoResult() {
        return anularPagoResult;
    }

    /**
     * Sets the value of the anularPagoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnularPagoResult(String value) {
        this.anularPagoResult = value;
    }

}
