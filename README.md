BlueprintDao
============
A light-weight, powerful and very customizable orm/dao framework.

<p>The <b>BlueprintDao</b> is a light-weight <b>JDBC</b> convenience layer. All that can be done in <b>JDBC</b>
can also be done on it. It attempts to overcome all the repetitive code, like setting parameters for statements or for retrieving queries; and provide useful tools to build statements, access all entities instances values and properties, transactions and so on.</p>

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
the example above, a determined <tt>SetType</tt> will be filled with the respective enums values:<br/>
<tt>[TRAILERS, DELETED_SCENES, COMMENTARIES]</tt></p>

<h5>Mapping inherited entities with annotations:</h5>
<p>When an entity is inherited, the <tt>@EntityID</tt> is annotated above the class.</p>
<pre>
@EntityTable("customer")
@EntityID("customer_id")
public class Customer extends Person {
	
	@EntityColumn("company")
	private String company;

	...
</pre>

======================
<h4>Starting a Session:</h4>
<p>A session is created for an more efficient management of the created daos, all of them will use the same connection and will share <tt>PrepareStatement</tt> mappings. A <tt>PreparedStatement</tt> is never created twice in a session. The <tt>SessionManager</tt> superclass will also control all the created entities and <tt>ResultSet</tt> mapped columns.</p>

<p>When a session is ended, all the created <tt>PreparedStatement</tt> instances are closed, as the <tt>Connection</tt> passed as parameter. So a session should be created in manner that all the queries and transactions uses it.</p>

<p>Below is a simple example of beginning and ending a session:</p>
<pre>
ConnectionFactory factory = new ConnectionFactory(); //your connection factory
Session session = new Session(factory.getConnection());

session.begin();
//queries and transactions here
session.end();
</pre>

<h5>Starting a Transaction:</h5>
<p>Inside of a session scope, transactions are performed.
Below is an example of a simple transaction.</p>

<pre>
try {
	Transaction transaction = session.transaction();
	transaction.begin();
	
	Person person = new Person();
	person.setName("xxxx xxxxxx");
	personDao.useSequence("sq_person")
	personDao.save(person); //generate and fill the id
	
	Customer customer = new Customer();
	customer.setId(person.getId());
	customer.setCompany("xxxxxxxx");
	customerDao.save(customer);
	
	transaction.end();
} catch (TransactionException e) {
	transaction.rollback();
}
</pre>

========================
<h4>Supported types:</h4>
<p>Moreover, the <b>BlueprintDao</b> supports all the common types, like <tt><b><i>java.lang</i></b></tt> types and the <tt><b><i>java.sql</i></b></tt> types. Below is an list of the supported Java types:</p>

<dl>
	<dt><tt>java.lang</tt></dt>
	<dd><tt>Long/long, Integer/int, Double/double, Float/float, Short/short, Byte/byte, Boolean/boolean</tt>
	<tt>Enum/enum, String.</tt></dd>
	
	<dt><tt>java.math</tt></dt>
	<dd><tt>BigDecimal</tt></dd>
	
	<dt><tt>java.util</tt></dt>
	<dd><tt>Date</tt></dd>
	
	<dt><tt>java.sql</tt></dt>
	<dd><tt>Date, Blob, Clob, NClob, Ref, RowId, SQLXML, Time and Timestamp</tt></dd>
	
	<dt><tt>java.net</tt></dt>
	<dd><tt>URL</tt><</dd>
	
	<dt><tt>blueprint.type</tt></dt>
	<dd><tt>SetType and EnumType</tt></dd>
</dl>









... to be continued.

<p>Requires Java 7 and above.</p>
