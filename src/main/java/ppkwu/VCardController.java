package ppkwu;

import ezvcard.VCard;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VCardController {

    private static final String WEEIA_EMPLOYEE_ENDPOINT = "https://adm.edu.p.lodz.pl/user/users.php?search=";

    @GetMapping("/ppkwu/vcard/{username}")
    @ResponseBody
    public ResponseEntity<?> getWeeiaCalendar(@PathVariable String username) {
        try {
            String weeiaPage = findEmployees(username);
            Elements userProfiles = filterUserProfile(weeiaPage);
            userProfiles.forEach(user ->{
                user.select(".btn").remove();
                user.append("<button type=\"button\">show Vcard</button>");
            });

           // VCard vCard = parseToICal(getWeeiaCalendarEvents(year,month), year, month);
            return new ResponseEntity<>(userProfiles.toString(), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("User in not found", HttpStatus.NOT_FOUND);
        }
    }

    private Elements filterUserProfile(String weeiaPage){
        Document doc = Jsoup.parse(weeiaPage);
        return doc.select("div.user-info");
    }

    private String findEmployees(String username) throws IOException {
        StringBuilder urlAddress = new StringBuilder(WEEIA_EMPLOYEE_ENDPOINT)
                .append(username)
                .append("&x=0&y=0");
        URL url = new URL(urlAddress.toString());
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        StringBuilder responseMessage = new StringBuilder();
        BufferedReader br;
        if(connection.getResponseCode() == 200){
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            responseMessage.append(br.lines().collect(Collectors.joining()));
        }else{
            responseMessage.append("error in loading weeia");
        }
        return responseMessage.toString();
    }

}
