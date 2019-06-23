package com.example.demo.TestContoller;


import com.example.demo.Entities.Person;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.bouncycastle.asn1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("test/")
public class ExampleController {



    @Autowired
    private XmlMapper xmlMapper;


    CertificateFactory cf;
    X509Certificate certificate;
    DLSequence seq;

    @GetMapping("first")
    public String getTheFirst() throws JsonProcessingException {
        Person p = new Person();
        p.setName("Nikos");
        p.setSurName("Matsaplokos");
        p.setType("Fiatros");
        return xmlMapper.writeValueAsString(p);
    }

    @GetMapping(value = "reply",consumes = "application/xml",produces = "application/xml")
    public String getMe(@RequestBody String xml) throws IOException {
        Person p = xmlMapper.readValue(xml,Person.class);
        return xmlMapper.writeValueAsString(p);
    }


    @GetMapping("cert")
    public String returnRoleFromCert(@RequestBody MultipartFile file) throws IOException, CertificateException {
        ASN1Primitive derObject = null;
        String path = System.getProperty("user.dir");
        File testFile = new File(path + File.separator + file.getOriginalFilename());
        file.transferTo(testFile);
        cf = CertificateFactory.getInstance("X509");
        byte[] test = DatatypeConverter.parseBase64Binary(inputToString(testFile));
        ByteArrayInputStream inputStrream = new ByteArrayInputStream(test);
        certificate = (X509Certificate) cf.generateCertificate(inputStrream);
        byte[] extensionValue = certificate.getExtensionValue("1.3.6.1.5.5.7.1.3");
        if(extensionValue != null){
            derObject = toDERObject(extensionValue);
            if(derObject instanceof DEROctetString){
                DEROctetString derOctetString = (DEROctetString) derObject;
                derObject = toDERObject(derOctetString.getOctets());
                if(derObject instanceof DERUTF8String){
                    DERUTF8String s = DERUTF8String.getInstance(derObject);
                    String encoded  = s.getString();
                    System.out.print(encoded);
                }else{
                    seq = (DLSequence) derObject;
                }
            }
        }
        ASN1Encodable encodable = seq.getObjectAt(0);
        ASN1Primitive embObj = encodable.toASN1Primitive();
        DLSequence seq1 = (DLSequence) embObj;
        ASN1Encodable encodable2 = seq1.getObjectAt(1);
        ASN1Primitive embObj2 = encodable2.toASN1Primitive();
        DLSequence seq2 = (DLSequence) embObj2;
        ASN1Encodable encodable3 = seq2.getObjectAt(0);
        ASN1Primitive embObj3 = encodable3.toASN1Primitive();
        DLSequence seq3 = (DLSequence) embObj3;
        ASN1Encodable encodable4 = seq3.getObjectAt(0);
        ASN1Primitive embObj4 = encodable4.toASN1Primitive();
        DLSequence seq4 = (DLSequence) embObj4;
        ASN1Encodable encodable5 = seq4.getObjectAt(1);
        ASN1Primitive embObj5 = encodable5.toASN1Primitive();
        DERUTF8String roleOfPSD2 = (DERUTF8String) embObj5;
        System.out.print("Role of PSD2 directive"+roleOfPSD2);
        return roleOfPSD2.toString();
    }

    private ASN1Primitive toDERObject(byte[] data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ASN1InputStream asn1In = new ASN1InputStream(in);
        return asn1In.readObject();
    }


    private String inputToString(File testFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString()
                    .replaceAll("-----BEGIN CERTIFICATE-----","")
                    .replaceAll("-----END CERTIFICATE-----","");
        } finally {
            reader.close();
        }
    }

}
