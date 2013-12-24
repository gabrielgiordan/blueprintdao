BlueprintDao
============
#Uma Breve Introdução
**BlueprintDao** é uma leve camada **JDBC**. Tudo que pode ser feito em **JDBC**, pode ser feito nele. Tem como meta, substituir todo código repetitivo do **JDBC**, como setar parâmetros para o **PreparedStatement** ou para o **ResultSet**; Além disso, disponibiliza utilitários para construir sua **SQL**, acessar os valores das instâncias das entidades, fazer transações e assim por diante.

###Mapeando entidades com anotações:
A **BlueprintDao** framework reconhece uma tabela pelo valor anotado na  anotação `@EntityTable`.

Uma identidade é marcada pela anotação `@EntityID`;
Colunas com a `@EntityColumn`; 
Chaves Estrangeiras com `@EntityObject`; 
e outras tabelas com associação com a `@EntityList`.

Segue abaixo um exemplo de uma classe bean utilizando a **BlueprintDao**:
```java
@EntityTable("film")
public class Filme {

	@EntityID("film_id")
	private int id;

	//se o nome da coluna é o mesmo da variável, o valor passado na anotação é desnecessário.
	@EntityColumn 
	private String title;
	
	@EntityObject("language_id")
	private Language language;
	
	@EntityObject("original_language_id")
	private Language originalLanguage;
	
	@EntityColumn("special_features")
	private SetType<SpecialFeatures> specialFeatures; // tipo set do MySQL
	
	@EntityList
	private List<FilmActor> actors;
```
A coluna `special_features` contém a seguinte set do **MySQL**:

`set('Trailers', 'Commentaries', 'Deleted Scenes', 'Behind the Scenes')`

No Java, o `SpecialFeatures` é um enumerator que implementa a interface `EnumType` para se tornar detectável em futuras consultas e alterações no banco de dados. Se uma consulta for rodada no exemplo acima, um `SetType` de um determinado objeto será preenchido com os valores:

`[TRAILERS, DELETED_SCENES, COMMENTARIES]`

###Mapeando entidades herdadas com anotações

Quando uma entidade é herdada, o `@EntityID` é anotado acima da classe.
```java
@EntityTable("customer")
@EntityID("customer_id")
public class Customer extends Person {
	
	@EntityColumn
	private String company;
```

#A classe Blueprint

Todas as classes DAO devem herdar a classe **_`Blueprint`_**. Essa classe contém métodos utilitários com o modificador de acesso `protected` para a construção de uma classe DAO. Abaixo um exemplo de uma classe DAO customizada:

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

###O ResultSetListener

Suponha que você queira fazer qualquer coisa a cada linha retornada em uma consulta; o **ResultSetListener** foi criado com este propósito. Ele retorna o ResultSet e o Objeto a cada iteração de linha. Por exemplo, suponha que você tem uma coluna não anotada e deseja obtê-la:

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

#A classe BlueprintDao

A classe **_`BlueprintDao`_** herda a classe abstrata **_`Blueprint`_**, 
então ela também pode ser utilizada para construir sua DAO. **_`BlueprintDao`_** é uma DAO já preparada que contém todos os métodos **CRUD** de uma DAO comum.

Para usa-la é simples:
```java
BlueprintDao<Person> personDao = new BlueprintDao<Person>(session) {};

session.begin();
for(Person person : personDao.list()) {
	//fazer algo
}
session.end();
```

Essa classe suporta ID do tipo ´String´ ou qualquer valor numérico suportado pela framework:
```java 
Country country = countryDao.search("USA");
```

Se você usa **Oracle**, uma sequência pode ser setada e utilizada dentro de um escopo de transação:
```java
personDao.useSequence("sq_person");
```

Em outros bancos de dados, como **MySQL**, você apenas solicita o uso de um auto incremento:
```java
personDao.useIncrement(true);
```

Quando uma sequência ou um auto incremento é usado, o método `personDao.save(person)` irá gerar e automaticamente setar o ID para a instância do objeto.

#A classe Session

Uma sessão é criada para um controle mais eficiente dos DAOs criados, todos eles, irão usar a mesma conexão e irão compartilhar mapeamentos de objetos da classe `PreparedStatement`. Um `PreparedStatement` nunca é instanciado duas vezes em uma sessão. A superclasse  `SessionManager` também irá controlar todas as entidades criadas e as colunas mapeadas do `ResultSet`.

Quando uma sessão é finalizada, todos as instâncias do `PreparedStatement` são fechadas, assim como a `Connection` passada como parâmetro. Então, uma sessão deve ser criada de maneira que todas as consultas e transações a usem.

Segue abaixo um simples exemplo para começar e terminar uma sessão:
```java
ConnectionFactory factory = new ConnectionFactory(); //sua classe connection factory
Session session = new Session(factory.getConnection());

session.begin();
//consultas e transações aqui
session.end();
```

###Começando uma Transação:

Dentro de um escopo de sessão, transações são efetuadas.

Abaixo um exemplo de uma simples transação:
```java
try {
	Transaction transaction = session.transaction();
	transaction.begin();
	
	Person person = new Person();
	person.setName("xxxx xxxxxx");
	personDao.useSequence("sq_person")
	personDao.save(person); //gera e preenche o ID
	
	Customer customer = new Customer();
	customer.setId(person.getId());
	customer.setCompany("xxxxxxxx");
	customerDao.save(customer);
	
	transaction.end();
} catch (TransactionException e) {
	transaction.rollback();
}
```

#A Engine

A classe **_`Engine`_** possui uma maneira diferente de executar consultas do que outras frameworks, todas as classes DAO, irão fazer uso dela. Quando uma consulta é executada, a engine irá preencher as variáveis do ID e das colunas dos objetos instanciados, enquanto irá associar a chave estrangeira ao objeto instanciado. Quando terminada, uma subsequente consulta é executada automaticamente, com apenas as chaves estrangeiras de valores não repetidos e seus respectivos objetos, com seus objetos estrangeiros.

Por exemplo, a tabela `city` possui uma chave estrangeira que identifica um `country`, então se você executar uma consulta `SELECT * FROM city`, a classe **_`Engine`_** irá executar uma subsequente consulta `SELECT * FROM country WHERE Code = ?`, porém, nunca repetindo o valor do parâmetro.

Isso pode ser verificado pelos hash codes dos objetos `Country` da resultante consulta:
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
O output será: 
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
Neste exemplo, uma consulta de 4079 cidades com seus respectivos países, levou 781ms para ser realizado, sem restrições, utilizando **MySQL**. Se a mesma consulta é repetida diversas vezes, o tempo desce para metade. Note que este valor pode variar por diversos motivos.

A base de dados `world` pode ser baixada no site oficial do [MySQL] (http://dev.mysql.com/doc/world-setup/en/index.html).

####Restringindo a Engine
Restrições também podem ser adicionadas para a entidade `country` quando executada uma consulta com a entidade `city`. Se algumas colunas ou objetos não são necessários, você pode obter a configuração da classe DAO:

```java
EntityObjectsSettings settings = cityDao.getObjectsSettings();
```

E adicionar restrições para as colunas:
```java
settings.restrictColumns(Country.class, "Continent", "LocalName");
```

Caso seja preferido, variáveis também podem ser restritas ao invés de colunas:
```java
settings.restrictFields(Country.class, "continent", "localName");
```

A classe **_`Country`_** também pode ser restringida totalmente:
```java
settings.restrictClass(Country.class);
```

Ou todos os objetos e seus sub objetos podem ser restringidos:
```java
settings.setFillSubObjects(false);
```
```java
settings.setFillObjects(false);
```

Então, quando escrevendo uma consulta, você não precisa utilizar joins para retornar colunas, mas sim, para realizar buscas.
Quando escrevendo um join, esteja atento de selecionar apenas as colunas da tabela atual, assim a busca será mais rápida.

Uma segunda alternativa à classe **_`Engine`_**  será escrita em futuras versões.

##Tipos Suportados

A **BlueprintDao** suporta todos os tipos comuns, como os contidos no pacote **_`java.lang`_** e no **_`java.sql`_**. 

Abaixo uma lista dos tipos suportados:

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

#Por Fim

A **BlueprintDao** ainda é um trabalho em progresso, nada pode ser garantido por enquanto. Qualquer sugestões, suporte e pedidos de colaboração, eu estarei agradecido em atender.
