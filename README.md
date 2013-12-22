BlueprintDao
============
<h2>A Brief Introduction</h2>
<p>The <b>BlueprintDao</b> is a light-weight <b>JDBC</b> convenience layer. All that can be done in <b>JDBC</b>
can also be done on it. It attempts to overcome all the repetitive code, like setting parameters for statements or for retrieving queries; and provide useful tools to build statements, access all entities instances values and properties, transactions and so on.</p>

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
	
	@EntityColumn //if the column name has the same field name, no value is needed
	private String title;
	
	@EntityObject("language_id")
	private Language language;
	
	@EntityObject("original_language_id")
	private Language originalLanguage;
	
	@EntityColumn("special_features")
	private SetType&lt;SpecialFeatures&gt; specialFeatures; // MySQL enum set type
	
	@EntityList
	private List&lt;FilmActor&gt; actors;
	
</pre>

<p>The <tt>special_features</tt> column contains the following <b>MySQL</b> set type:<br/>
<tt>set('Trailers', 'Commentaries', 'Deleted Scenes', 'Behind the Scenes')</tt></p>

<p>In Java, the <tt>SpecialFeatures</tt> is an enum that implements the <tt>EnumType</tt> 
interface to became detectable on futures queries and updates. If you run a query on
the example above, a determined <tt>SetType</tt> will be filled with the respective enums values:<br/>
<tt>[TRAILERS, DELETED_SCENES, COMMENTARIES]</tt>. For more details, the <b>JavaDoc</b> is included.</p>

<h5>Mapping inherited entities with annotations:</h5>
<p>When an entity is inherited, the <tt>@EntityID</tt> is annotated above the class.</p>
<pre>
@EntityTable("customer")
@EntityID("customer_id")
public class Customer extends Person {
	
	@EntityColumn
	private String company;
	
</pre>

<h2>The Blueprint</h2>
<p>All DAO classes should extend the <tt><b><i>Blueprint</i></b></tt> class. This class contains useful protected methods to build your DAO class. Below is an example of a custom DAO class:</p>

<pre>
public class CountryDao extends Blueprint&lt;Country&gt; {

	public CountryDao(Session session) {
		super(session);
	}
	
	public List&lt;Country&gt; listByContinent(Continent continent) {
		
		setStatement("SELECT * FROM country WHERE Continent = ?");
		
		addPlaceholderValue(continent); //enum
		
		return runSeveralRows();
	}
}
</pre>

<h4>The ResultSetListener</h4>
<p>Suppose you want to do whatever you want with each Country row in a query, so the <b>ResultSetListener</b> was created for this purpose. It returns the ResultSet and the Country on each row iteration. For example, suppose you have a non-mapped column and want to obtain it:</p>

<pre>
public List&lt;CountryRank&gt; getLifeExpectancyRank(int rankSize) {
		
	setStatement("SET @rank=0");
	runUpdate();
		
	setStatement("SELECT @rank:=@rank+1 AS rank, country.* " +
	             "FROM country ORDER BY LifeExpectancy DESC LIMIT 0, ?");
	             
	addPlaceholderValue(rankSize);
		
	final List&lt;CountryRank&gt; ranking = new ArrayList&lt;&gt;();
		
	nextSeveralRows(new ResultSetListener() {

		@Override
		protected void performAction(ResultSet resultSet, Country next) throws SQLException {
			
			CountryRank rank = new CountryRank();
			
			rank.setCountry(next);
			rank.setRank(resultSet.getInt("rank"));
			
			ranking.add(rank);
		}
	});
		
	return ranking;
}
</pre>

<h2>The BlueprintDao</h2>
<p>The <tt><b><i>BlueprintDao</i></b></tt> class extends the <tt><b><i>Blueprint</i></b></tt> abstract class, so you can also use it to build your data access object class. The <b>BlueprintDao</b> is a prepared DAO, that contains all the <b>CRUD</b> methods of a common DAO class.</p>
<p>To use it without extend is pretty simple:</p>
<pre>
BlueprintDao&lt;Person&gt; personDao = new BlueprintDao&lt;Person&gt;(session) {};

session.begin();
for(Person person : personDao.list()) {
	//do something
}
session.end();
</pre>
<p>This class also supports String identities or whatever numeric types supported by the framework:
<br/><tt>
Country country = countryDao.search("USA");
</tt></p>
<p>If you're working with <b>Oracle</b> databases, you can set the sequence that can be used inside a transaction scope:
<br/><tt>
personDao.useSequence("sq_person");
</tt></p>
<p>In other databases like <b>MySQL</b> you just request an auto-increment use:
<br/><tt>
personDao.useIncrement(true);
</tt></p>
<p>When a sequence or an increment is used, the <tt>personDao.save(person)</tt> method will generate and set the identity to the <tt>Person</tt> instance passed as parameter.</p>

<h2>The Session</h2>
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

<h2>The Engine</h2>
<p>The <tt><b><i>Engine</i></b></tt> class has a different way to execute queries than other frameworks, all DAO classes will make use of it. First, when a query is performed, the engine fill the identity and columns fields of instantiated objects while associating the foreign key to the instantiated object. When it's done, a subsequent query is performed automatically, with only the non-repeated foreign keys and their respective objects are filled with the foreign objects.</p>
<p>For example, the table <tt>city</tt> has a foreign key that identifies a <tt>country</tt>, so if you run a <tt>SELECT * FROM city</tt> query, the <tt><b><i>Engine</i></b></tt> will perform a subsequent <tt>SELECT * FROM country WHERE Code = ?</tt> query, but never repeating a value.</p>
<p>This can be verified printing all hash codes of <tt>Country</tt> objects of the resulted query.</p>
<p></p>
<pre>
for (City city : cityDao.list()) {
	System.out.println
	(
		"Country: " + city.getCountry().getName() + " - " + 
		"HashCode: " + city.getCountry().hashCode() + " - " +
		"City: " + city.getName()
	);
}
</pre>
<p>The output will be: </p>
<pre>
Country: Vietnam - HashCode: 146419630 - City: Vinh
Country: Vietnam - HashCode: 146419630 - City: My Tho
Country: Vietnam - HashCode: 146419630 - City: Da Lat
Country: Vietnam - HashCode: 146419630 - City: Buon Ma Thuot
Country: Estonia - HashCode: 2005945595 - City: Tallinn
Country: Estonia - HashCode: 2005945595 - City: Tartu
Country: United States - HashCode: 581840912 - City: New York
Country: United States - HashCode: 581840912 - City: Los Angeles
Country: United States - HashCode: 581840912 - City: Chicago
Country: United States - HashCode: 581840912 - City: Houston
Country: United States - HashCode: 581840912 - City: Philadelphia
...
</pre>
<p>In this example, a query of 4079 cities with their respective countries, took 2.1 seconds to be done without restrictions using <b>MySQL</b>.<br/>
The <tt>world</tt> database can be downloaded at the official <a href="http://dev.mysql.com/doc/world-setup/en/index.html"><b>MySQL</b></a> site.</p>

<h4>Restricting the Engine</h4>
<p>Restrictions can also be added to the <tt>country</tt> entity when making a <tt>city</tt> query. If some columns or objects aren't needed, you can get the restrictions configuration of concerning the DAO class:</p>

<tt>EntityObjectsSettings settings = cityDao.getObjectsSettings();</tt>

<p>And set restrictions to columns:</p>
<tt>settings.restrictColumns(Country.class, "Continent", "LocalName");</tt>

<p>If preferred, fields can be restricted instead of columns:</p>
<tt>settings.restrictFields(Country.class, "continent", "localName");</tt>

<p>The <tt><b><i>Country</i></b></tt> class can also be entirely restricted:</p>
<tt>settings.restrictClass(Country.class);</tt>

<p>Or all objects and its sub objects can be restricted:</p>
<tt>settings.setFillSubObjects(false);</tt><br/>
<tt>settings.setFillObjects(false);</tt>

<p>So, when writing a query, you don't need to use joins to return columns, but to perform searches. 
When writing a join, be aware to select only the current table columns, so the query will be faster.</p>

<h2>Supported Types</h2>
<p>Moreover, the <b>BlueprintDao</b> supports all the common types, like <tt><b><i>java.lang</i></b></tt> types and the <tt><b><i>java.sql</i></b></tt> types. Below is an list of the supported types:</p>

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

<h2>Finally</h2>
The <b>BlueprintDao</b> is still in progress, so I do not guarantee anything.<br/>
Any grammar errors, contact me. My native language is Portuguese.<br/>
Also any suggestions, support and collaboration requests, I'll be very grateful to attend.
<p>Greetings from Brazil!</p>
