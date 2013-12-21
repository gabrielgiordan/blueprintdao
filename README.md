BlueprintDao
============

A simple, powerful and very customizable orm/dao framework.

pt-br

<p>Requer Java 7 ou superior</p>

<h2>Mapeando uma classe bean com annotations</h2>

<pre>
@<span class="ST0">EntityTable</span>(<span class="character">&quot;</span><span class="character">film</span><span class="character">&quot;</span>)
<span class="keyword-directive">public</span> <span class="keyword-directive">class</span> <span class="ST1">Film</span> {

        @<span class="ST0">EntityID</span>(<span class="character">&quot;</span><span class="character">film_id</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> <span class="keyword-directive">int</span> <span class="ST2">id</span>;
        
        @<span class="ST0">EntityColumn</span>(<span class="character">&quot;</span><span class="character">title</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> String <span class="ST2">title</span>;
        
        @<span class="ST0">EntityColumn</span>(<span class="character">&quot;</span><span class="character">release_year</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> <span class="keyword-directive">short</span> <span class="ST2">releaseYear</span>;
        
        @<span class="ST0">EntityObject</span>(<span class="character">&quot;</span><span class="character">language_id</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> <span class="ST0">Language</span> <span class="ST2">language</span>;

        @<span class="ST0">EntityColumn</span>(<span class="character">&quot;</span><span class="character">rating</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> <span class="ST0">Rating</span> <span class="ST2">rating</span>;
        
        @<span class="ST0">EntityColumn</span>(<span class="character">&quot;</span><span class="character">special_features</span><span class="character">&quot;</span>)
        <span class="keyword-directive">private</span> <span class="ST0">SetType</span>&lt;<span class="ST0">SpecialFeatures</span>&gt; <span class="ST2">specialFeatures</span>;
        
        @<span class="ST0">EntityList</span>
<span class="keyword-directive">        private</span> List&lt;<span class="ST0">FilmActor</span>&gt; <span class="ST2">actors</span>;
</pre>
