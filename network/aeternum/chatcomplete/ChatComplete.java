package network.aeternum.chatcomplete;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A simple chat tab-completer using Google search's auto complete API
 * 
 * @author BananaPuncher714
 */
public class ChatComplete extends JavaPlugin implements Listener {
	public static final int PHRASE_LENGTH = 3;
	public static final boolean RETURN_IF_COMPLETIONS_EXIST = false;
	public static final boolean FIRST_WORD_ONLY = true;
	
	/*
				   _____                   _                 
				  / ____|                 | |                
				 | |  __  ___   ___   __ _| | ___            
				 | | |_ |/ _ \ / _ \ / _` | |/ _ \           
				 | |__| | (_) | (_) | (_| | |  __/           
				  \_____|\___/ \___/ \__, |_|\___|   _       
				  / ____|             __/ |  | |    | |      
				 | |     ___  _ __ __|___/__ | | ___| |_ ___ 
				 | |    / _ \| '_ ` _ \| '_ \| |/ _ \ __/ _ \
				 | |___| (_) | | | | | | |_) | |  __/ ||  __/
				  \_____\___/|_| |_| |_| .__/|_|\___|\__\___|
				                       | |                   
				                       |_|                         
	*/
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( this, this );
	}
	
	@EventHandler
	public void onPlayerChatTabComplete( PlayerChatTabCompleteEvent event ) {
		if ( !event.getPlayer().hasPermission( "chatcomplete.use" ) ) {
			return;
		}
		String message = event.getChatMessage();
		if ( RETURN_IF_COMPLETIONS_EXIST && !event.getTabCompletions().isEmpty() ) {
			return;
		}
		String filtered = message.replaceAll( "[^\\s|\\w]", "" );
		String phrase = trimPhrase( filtered, PHRASE_LENGTH );
		String token = event.getLastToken().replaceAll( "[^\\s|\\w]", "" );
		for ( String completion : query( phrase ) ) {
			if ( completion.startsWith( phrase ) ) {
				String firstWord = token + completion.substring( phrase.length() );
				if ( FIRST_WORD_ONLY ) {
					firstWord = firstWord.split( "\\s+" )[ 0 ];
				}
				if ( !event.getTabCompletions().contains( firstWord ) ) {
					event.getTabCompletions().add( firstWord );
				}
			}
		}
	}
	
	/**
	 * Trims a phrase to a certain number of words
	 * 
	 * @param phrase
	 * The phrase to be trimmed; Captures the last few words
	 * @param words
	 * The amount of words to capture
	 * @return
	 * The newly trimmed string
	 */
	public static String trimPhrase( String phrase, int words ) {
		String[] split = phrase.split( "\\s+" );
		StringBuilder queryBuilder = new StringBuilder();
		for ( int i = Math.max( 0, split.length - words ); i < split.length; i++ ) {
			queryBuilder.append( split[ i ].toLowerCase() );
			queryBuilder.append( " " );
		}
		return phrase.endsWith( " " ) ? queryBuilder.toString() : queryBuilder.toString().trim();
	}
	
	/**
	 * Uses Google search's auto complete system to provide auto complete options
	 * 
	 * @param phrase
	 * The phrase to use; Shorter phrases may yield more results
	 * @return
	 * Up to 10 results that may or may not contain the phrase 
	 */
	public static List< String > query( String phrase ) {
		List< String > results = new ArrayList< String >();
		if ( phrase == null || phrase.matches( "\\s*" ) ) {
			return results;
		}
		try {
			URL fetch = new URL( "http://google.com/complete/search?q=" + phrase.replaceAll( "[^\\s|\\w]", "" ).replaceAll( "\\s", "%20" ) + "&output=toolbar" );
			URLConnection connection = fetch.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

			Scanner scanner = new Scanner( connection.getInputStream() );
			scanner.useDelimiter( "\\Z" );
			for ( String match : getMatches( scanner.next(), "data=\"(.*?)\"" ) ) {
				results.add( StringEscapeUtils.unescapeHtml4( match ) );
			}
			scanner.close();
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
		return results;
	}
	
	private static List< String > getMatches( String string, String regex ) {
		Pattern pattern = Pattern.compile( regex );
		Matcher matcher = pattern.matcher( string );
		List< String > matches = new ArrayList< String >();
		while ( matcher.find() ) {
			matches.add( matcher.group( 1 ) );
		}
		return matches;
	}
}
