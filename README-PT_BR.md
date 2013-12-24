BlueprintDao
============
#A Brief Introduction
The **BlueprintDao** is a light-weight **JDBC** convenience layer. All that can be done in **JDBC**
can also be done on it. It attempts to overcome all the repetitive code, like setting parameters for statements or for retrieving queries; and provide useful tools to build statements, access all entities instances values and properties, transactions and so on.

###Mapping entities with annotations:
The **BlueprintDao** framework recognizes a table by the value
annotated on an `@EntityTable` annotation.

An identity is marked by an `@EntityID` annotation;
Columns with `@EntityColumn` annotations; 
Foreigners with `@EntityObject` annotations; 
and other tables with `@EntityList` annotations without parameters.

Below is an example of a bean class using the **BlueprintDao** framework:
```java
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
	private SetType<SpecialFeatures> specialFeatures; // MySQL enum set type
	
	@EntityList
	private List<FilmActor> actors;
```
The `special_features` column contains the following **MySQL** set type:

`set('Trailers', 'Commentaries', 'Deleted Scenes', 'Behind the Scenes')`

In Java, the `SpecialFeatures` is an enum that implements the `EnumType` interface to became detectable on futures queries and updates. If you run a query on the example above, a determined `SetType` will be filled with the respective enums values: 

`[TRAILERS, DELETED_SCENES, COMMENTARIES]`

###Mapping inherited entities with annotations:

When an entity is inherited, the `@EntityID` is annotated above the class.
```java
@EntityTable("customer")
@EntityID("customer_id")
public class Customer extends Person {
	
	@EntityColumn
	private String company;
```

#The Blueprint

All DAO classes should extend the **_`Blueprint`_** class. This class contains useful protected methods to build your DAO class. Below is an example of a custom DAO class:
```java
public class CountryDao extends Blueprint<Country> {

	public CountryDao(Session session) {
		super(session);
	}
	
	public List<Country> listByContinent(Continent continent) {
		
		setStatement("SELECT * FROM country WHERE Continent = ?");
		
		addPlaceholderValue(continent); //enum
		
		return runSeveralRows();
	}
}
```

###The ResultSetListener

Suppose you want to do whatever you want with each Country row in a query, so the **ResultSetListener** was created for this purpose. It returns the ResultSet and the Country on each row iteration. For example, suppose you have a non-mapped column and want to obtain it:
```java
public List<CountryRank> getLifeExpectancyRank(int rankSize) {
		
	setStatement("SET @rank=0");
	runUpdate();
		
	setStatement("SELECT @rank:=@rank+1 AS rank, country.* " +
	             "FROM country ORDER BY LifeExpectancy DESC LIMIT 0, ?");
	             
	addPlaceholderValue(rankSize);
		
	final List<CountryRank> ranking = new ArrayList<>();
		
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
```

#The BlueprintDao

The **_`BlueprintDao`_** class extends the **_`Blueprint`_** abstract class, so you can also use it to build your data access object class. The **_`BlueprintDao`_** is a prepared DAO, that contains all the **CRUD** methods of a common DAO class.

To use it without extend is pretty simple:
```java
BlueprintDao<Person> personDao = new BlueprintDao<Person>(session) {};

session.begin();
for(Person person : personDao.list()) {
	//do something
}
session.end();
```

This class also supports String identities or whatever numeric types supported by the framework:
```java 
Country country = countryDao.search("USA");
```

If you're working with **Oracle** databases, you can set the sequence that can be used inside a transaction scope:
```java
personDao.useSequence("sq_person");
```

In other databases like **MySQL** you just request an auto-increment use:
```java
personDao.useIncrement(true);
```

When a sequence or an increment is used, the `personDao.save(person)` method will generate and set the identity to the `Person` instance passed as parameter.

#The Session

A session is created for an more efficient management of the created daos, all of them will use the same connection and will share `PrepareStatement` mappings. A `PreparedStatement` is never created twice in a session. The `SessionManager` superclass will also control all the created entities and `ResultSet` mapped columns.

When a session is ended, all the created `PreparedStatement` instances are closed, as the `Connection` passed as parameter. So a session should be created in manner that all the queries and transactions uses it.

Below is a simple example of beginning and ending a session:
```java
ConnectionFactory factory = new ConnectionFactory(); //your connection factory
Session session = new Session(factory.getConnection());

session.begin();
//queries and transactions here
session.end();
```

###Starting a Transaction:

Inside of a session scope, transactions are performed.

Below is an example of a simple transaction.
```java
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
```

#The Engine

The **_`Engine`_** class has a different way to execute queries than other frameworks, all DAO classes will make use of it. When a query is performed, the engine fill the identity and columns fields of instantiated objects while associating the foreign key to the instantiated object. When it's done, a subsequent query is performed automatically, with only the non-repeated foreign keys and their respective objects are filled with the foreign objects.

For example, the table `city` has a foreign key that identifies a `country`, so if you run a `SELECT * FROM city` query, the **_`Engine`_** will perform a subsequent `SELECT * FROM country WHERE Code = ?` query, but never repeating a parameter value.

This can be verified printing all hash codes of `Country` objects of the resulted query.
```java
for (City city : cityDao.list()) {
	System.out.println
	(
		"Country: " + city.getCountry().getName() + " - " + 
		"HashCode: " + city.getCountry().hashCode() + " - " +
		"City: " + city.getName()
	);
}
```
The output will be: 
```
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
```
In this example, a query of 4079 cities with their respective countries, took 781ms to be done without restrictions using **MySQL**. If the same query is repeated several times again, the time decreases to half.

The `world` database can be downloaded at the official [MySQL] (http://dev.mysql.com/doc/world-setup/en/index.html) site.

####Restricting the Engine

Restrictions can also be added to the `country` entity when making a `city` query. If some columns or objects aren't needed, you can get the restrictions configuration of the concerning DAO class:

```java
EntityObjectsSettings settings = cityDao.getObjectsSettings();
```

And set restrictions to columns:
```java
settings.restrictColumns(Country.class, "Continent", "LocalName");
```

If preferred, fields can be restricted instead of columns:
```java
settings.restrictFields(Country.class, "continent", "localName");
```

The **_`Country`_** class can also be entirely restricted:
```java
settings.restrictClass(Country.class);
```

Or all objects and its sub objects can be restricted:
```java
settings.setFillSubObjects(false);
```
```java
settings.setFillObjects(false);
```

So, when writing a query, you don't need to use joins to return columns, but to perform searches. 
When writing a join, be aware to select only the current table columns, so the query will be faster.

A second **_`Engine`_** alternative will be writed in future releases.

##Supported Types

Moreover, the **BlueprintDao** supports all the common types, like **_`java.lang`_** types and the **_`java.sql`_** types. 

Below is an list of the supported types:

| **_`java.lang`_**	| **_`java.math`_** | **_`java.util`_** | **_`java.sql`_**  | **_`java.net`_** | **_`blueprint.type`_** |
| :-------------------: |:-------------:|:---------:|:---------:|:--------:|:--------------:|
| Long/long 		| BigDecimal 	| Date	    | Date 	| URL	   | SetType	    |
| Integer/int 		|		|           | Blob	|          | EnumType	    |
| Double/double 	|		|           | Clob	|          |		    |
| Float/float 		|		|           | NClob	|          |		    |
| Short/short 		|		|           | Ref	|          |		    |
| Byte/byte 		|		|           | RowId	|          |		    |
| Boolean/boolean 	|		|           | SQLXML	|          |		    |
| String 		|		|           | Time	|          |		    |
| enum 			|		|           | Timestamp |          |		    |

#Finally

The **BlueprintDao** is still in progress, so I cannot guarantee anything.
Any grammar errors, contact me. My native language is Portuguese.
Also any suggestions, support and collaboration requests, I'll be very grateful to attend.

Greetings from Brazil!
