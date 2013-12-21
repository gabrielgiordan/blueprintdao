BlueprintDao
============
An light-weight, powerful and very customizable orm/dao framework.

<h2>Getting Started</h2>

<h4>Mapping entities with annotations:</h4>
<p>The <b>BlueprintDao</b> framework recognizes a table by the value
annotated on an <tt>@EntityTable</tt> annotation.</p>

<p>An identity is recognized by an <tt>@EntityID</tt> annotation.
Same as columns with <tt>@EntityColumn</tt> annotations, foreigners with 
<tt>@EntityObject</tt> annotations and also other tables 
with <tt>@EntityList</tt> annotations with no parameters.<br/>
The generic type class of the list annotated with an <tt>@EntityList</tt> should 
contain an identity that associate the current class being annotated.</p>

<p>Below is an example of a bean class using the <b>BlueprintDao</b> framework:</p>

<pre>
@EntityTable("film")
public class Film {

	@EntityID("film_id")
	private int id;
	
	@EntityColumn("title")
	private String title;
	
	@EntityObject("language_id")
	private Language language;
	
	@EntityObject("original_language_id")
	private Language originalLanguage;
	
	@EntityColumn("rating")
	private Rating rating; // Enum type
	
	@EntityColumn("special_features")
	private SetType&lt;SpecialFeatures&gt; specialFeatures; // MySQL enum set type
	
	@EntityList
	private List&lt;FilmActor&gt; actors;
	
	...
</pre>

<p>The <tt>special_features</tt> column contains the following MySQL set type:<br/>
<tt>set('Trailers', 'Commentaries', 'Deleted Scenes', 'Behind the Scenes')</tt></p>

<p>In Java, the <tt>SpecialFeatures</tt> is an enum that implements the <tt>EnumType</tt> 
interface to became detectable on futures queries and updates. If you run a query on
the table above, the <tt>SetType</tt> will be filled with the respective enums values:<br/>
<tt>[TRAILERS, DELETED_SCENES, COMMENTARIES]</tt></p>

... to be continued.

<p>Requires Java 7 and above.</p>
