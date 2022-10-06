import java.util.ArrayList;

//Command struct
public class Message {
	// this is my work on the client class
	//private String input;
	private String type;
	private String responseType;
	//private int id;
	private ArrayList<String> headers;
	private String body;
	private String responseBody;
	
	//private BufferedReader in;
	//private PrintWriter out;
	
	public Message(String type, String body, int id) {
		this.type = type;
		headers = new ArrayList<String>();
		headers.add(Integer.toString(id));
		this.body = body;
		this.responseType = type;
		this.responseBody = body;
		
	}

	
	public Message(String input, int id) {
		//this.input = input;
		headers = new ArrayList<String>();
		headers.add(Integer.toString(id));
		
		String inputArr[] = input.split(" ", 2);
		//System.out.println("message_input: " + inputArr[0]);
		try {
			this.type = inputArr[0].substring(5);
			this.body = inputArr[1];
			this.responseType = ""; 
			this.responseBody = ""; 
			
			if (type.equals("CommandCommand") || type.equals("Command")) {
				this.type = "Command";
				
				String bodyArr[] = body.split(" ");
				body = bodyArr[0];
				
				switch(bodyArr[0]) {
				case "connect":
					headers.add(bodyArr[3]); //add username to headers
					responseType = "History";
					responseBody = "Connected to Texas Holdem Poker main lobby";					
					break;
				case "bet":
					responseType = "History";
					headers.add(bodyArr[1]);
					break;
				case "call":
					responseType = "History";
					break;
				case "fold":
					responseType = "History";
					break;
				case "exit": 
					responseType = "History";
					break;
				case "creategame":
					responseType = "History";
					break;
				case "joingame":
					responseType = "History";
					headers.add(bodyArr[1]);
					//System.out.println("body: " + body);
					break;
				case "ready":
					responseType = "History";
					//responseBody = "players ready: ";
					break;
				case "listgames":
					responseType = "History";
					
					
				default:
					responseType = "History";
					responseBody = "Unknown command";
				}
			} else if (type.equals("ChatChat") || type.equals("Chat")) {
				//String bodyArr[] = body.split(" ", 2);
				responseType = "Chat";
				responseBody = body;
			}
			
			
		} catch (Exception e) {
			this.type = "fail";
			body = null;
			e.printStackTrace();
		}
		//this.type = inputArr[0];
		//System.out.println("type: " + this.type);
		
		//if (inputArr.length > 1) this.body = inputArr[1]; //for testing, to be changed
		

	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ArrayList<String> getHeaders() {
		return headers;
	}
	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}
	public String getBody() {
		return body;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setBody(String body) {
		this.responseBody = body;
	}

	public String sendResponse(int length) {
		return "Message:" + Integer.toString(length) + "\nType:" + this.responseType + "\n\n" + this.responseBody;
	}
	public String sendResponse(int length, String addition) {
		return "Message:" + Integer.toString(length) + "\nType:" + this.responseType + "\n\n" + this.responseBody + addition;
	}
	public String sendResponse(String response) {
		return response;
	}
	
}
