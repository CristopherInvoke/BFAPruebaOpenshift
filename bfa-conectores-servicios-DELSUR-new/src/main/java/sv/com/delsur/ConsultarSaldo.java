
package sv.com.delsur;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="pNisNpe" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/&gt;
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
    "pNisNpe"
})
@XmlRootElement(name = "Consultar_Saldo")
public class ConsultarSaldo {

    protected String pNisNpe;

    /**
     * Gets the value of the pNisNpe property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPNisNpe() {
        return pNisNpe;
    }

    /**
     * Sets the value of the pNisNpe property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPNisNpe(String value) {
        this.pNisNpe = value;
    }

}
