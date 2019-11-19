Title
Get weeia calendar page with information about the in format of vcard
•	URL
/ppkwu/vacrd/{username}
•	Method:
GET
•	URL Params
Username 
•	Success Response:
Page with employess name and clicable button to show vcard with their info
o	Code: 200
•	Error Response:
Not suported
o	Code: 404 NOT FOUND
Content: { error : "User not found" }
•	Sample Call:
http://localhost:8080/ppkwu/vcard/wajman
