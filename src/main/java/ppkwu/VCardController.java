package ppkwu;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Revision;
import ezvcard.property.StructuredName;
import ezvcard.property.Uid;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VCardController {

    private static final String WEEIA_EMPLOYEE_ENDPOINT = "https://adm.edu.p.lodz.pl/user/users.php?search=";
    private static final String WEEIA_PROFILE_ENDPOINT = "https://adm.edu.p.lodz.pl/user/profile.php?id=";

    @GetMapping("/ppkwu/search/{employee}")
    @ResponseBody
    public ResponseEntity<?> getWeeiaEmployees(@PathVariable String employee) {
        try {
            StringBuilder urlAddress = new StringBuilder(WEEIA_EMPLOYEE_ENDPOINT)
                    .append(employee)
                    .append("&x=0&y=0");
            String weeiaPage = fetchDocFile(urlAddress.toString());

            Elements userProfiles = filterByRegex(weeiaPage, "div.user-info");
            userProfiles.forEach(user -> {
                String profileLink = user.select("a").attr("href");
                String employeeId = profileLink.substring(profileLink.lastIndexOf("id=") + 3);
                user.select(".btn").remove();
                user.append("<button type=\"button\" onclick= \" window.location.href ='/ppkwu/vcard/" + employeeId + "'\">wygeneruj vCard</button>");
            });

            return new ResponseEntity<>(userProfiles.toString(), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("User in not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/ppkwu/vcard/{employeeId}")
    @ResponseBody
    public ResponseEntity<?> getEmployeeProfile(@PathVariable int employeeId) throws IOException {
        StringBuilder urlAddress = new StringBuilder(WEEIA_PROFILE_ENDPOINT)
                .append(employeeId);
        String weeiaPage = fetchDocFile(urlAddress.toString());

        Employee employee = fetchEmployeeData(weeiaPage);
        VCard vcard = new VCard();
        StructuredName n = new StructuredName();
        n.setFamily(employee.getLastName());
        n.setGiven(employee.getFirstName());
        vcard.setStructuredName(n);

        vcard.setFormattedName(employee.getFirstName() + " " + employee.getLastName());

        vcard.setUid(Uid.random());
        vcard.setRevision(Revision.now());

        return new ResponseEntity<>(Ezvcard.write(vcard).version(VCardVersion.V3_0).go(), HttpStatus.OK);
//        File f=new File("contact.vcf");
//        f.createNewFile();
//        FileOutputStream fop=new FileOutputStream(f);
//
//        VCard vcard = new VCard();
//
//        Employee employee = new Employee();
//        n.setFamily("Doe");
//        n.setGiven("Jonathan");
//        n.getPrefixes().add("Mr");
//        vcard.setStructuredName(n);
//
//        vcard.setFormattedName("John Doe");
//
//        Ezvcard.write(vcard).version(VCardVersion.V4_0).go(System.out);
    }

    private Employee fetchEmployeeData(String weeiaPage) {
        Employee employee = new Employee();
        Elements profileName = filterByRegex(weeiaPage, "div.profile-box > h2");
        String[] splited = profileName.text().split(" ");
        employee.setFirstName(splited[0]);
        employee.setLastName(splited[1]);

//        Elements profileEmail = filterByRegex(weeiaPage, "div.profile-container > ul > li.email");
//        employee.setEmail(profileEmail.text());

        return employee;
    }

    private Elements filterByRegex(String weeiaPage, String regex) {
        Document doc = Jsoup.parse(weeiaPage);
        return doc.select(regex);
    }

    private String fetchDocFile(String urlAddress) throws IOException {
        URL url = new URL(urlAddress);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        StringBuilder responseMessage = new StringBuilder();
        BufferedReader br;
        if (connection.getResponseCode() == 200) {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            responseMessage.append(br.lines().collect(Collectors.joining()));
        } else {
            responseMessage.append("error in loading weeia");
        }
        return responseMessage.toString();
    }

}
