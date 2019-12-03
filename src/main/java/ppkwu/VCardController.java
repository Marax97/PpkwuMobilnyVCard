package ppkwu;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Revision;
import ezvcard.property.StructuredName;
import ezvcard.property.Uid;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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

    @GetMapping("/ppkwu")
    @ResponseBody
    public String getWeeiaEmployees() {
        return "<body style=\"text-align:center;\">  \n" +
                "<form onSubmit=\"return find();\">\n" +
                " Name: <input type=\"text\" id=\"employee\"><br>\n" +
                "  <input type=\"submit\" value=\"Submit\">\n" +
                "</form>" +
                "    <script>  \n" +
                "        function find() { \n " +
                "            var txt = document.getElementById('employee').value; \n" +
                "            location = '/ppkwu/search/' + txt;\n}" +
                "    </script> " +
                "</body>  \n";
    }

    @GetMapping("/ppkwu/search/{employee}")
    @ResponseBody
    public ResponseEntity<?> form(@PathVariable String employee) {
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

    @RequestMapping(value = "/ppkwu/vcard/{employeeId}", method = RequestMethod.GET)
    public void getEmployeeProfile(@PathVariable("employeeId") int employeeId, HttpServletResponse response) throws IOException {
        StringBuilder urlAddress = new StringBuilder(WEEIA_PROFILE_ENDPOINT)
                .append(employeeId);
        String weeiaPage = fetchDocFile(urlAddress.toString());

        Employee employee = fetchEmployeeData(weeiaPage);
        VCard vcard = createVCard(employee);

        File file = new File("employee.vcf");
        Ezvcard.write(vcard).go(file);
        InputStream myStream = new FileInputStream(file);

        org.apache.commons.io.IOUtils.copy(myStream, response.getOutputStream());
        response.addHeader("Content-disposition", "attachment;filename=employee.vcf");
        response.setContentType("application/vcf");
    }

    private VCard createVCard(Employee employee) {
        VCard vcard = new VCard();
        StructuredName n = new StructuredName();
        n.setFamily(employee.getLastName());
        n.setGiven(employee.getFirstName());
        vcard.setStructuredName(n);

        vcard.setFormattedName(employee.getFirstName() + " " + employee.getLastName());

        vcard.setUid(Uid.random());
        vcard.setRevision(Revision.now());
        return vcard;
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
