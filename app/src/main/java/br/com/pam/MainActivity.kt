package br.com.pam

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val cream = Color.rgb(255, 248, 234)
    private val sand = Color.rgb(247, 235, 211)
    private val paper = Color.rgb(217, 184, 130)
    private val line = Color.rgb(230, 211, 179)
    private val feather = Color.rgb(123, 74, 38)
    private val ink = Color.rgb(74, 42, 16)
    private val soft = Color.rgb(138, 106, 76)

    private lateinit var lora: Typeface
    private lateinit var playfair: Typeface
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout

    private val worker = Executors.newSingleThreadExecutor()
    private val api = PoetryApi()

    private var generation = 0
    private var screen = Screen("home")
    private var history = arrayListOf<Screen>()
    private var featured: Poem? = null
    private var authorQuery = ""
    private var filters = Filters()
    private var quantity = 3

    data class Screen(
        val route: String,
        val name: String = "",
        val author: String = "",
        val tab: String = route,
        val filter: Filters = Filters(),
        val amount: Int = 3,
        var poems: List<Poem>? = null
    ) : java.io.Serializable

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        lora = resources.getFont(R.font.lora)
        playfair = resources.getFont(R.font.playfair)

        @Suppress("DEPRECATION", "UNCHECKED_CAST")
        if (state != null) {
            screen = state.getSerializable("screen") as? Screen ?: screen

            history =
                (state.getSerializable("history") as? ArrayList<Screen>)
                    ?: history

            filters = state.getSerializable("filters") as? Filters ?: filters
            quantity = state.getInt("quantity", 3)
            authorQuery = state.getString("authorQuery", "")
        }

        render()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)

        out.putSerializable("screen", screen)
        out.putSerializable("history", history)
        out.putSerializable("filters", filters)
        out.putInt("quantity", quantity)
        out.putString("authorQuery", authorQuery)
    }

    override fun onDestroy() {
        generation++
        worker.shutdownNow()
        super.onDestroy()
    }

    @Deprecated("Compatibilidade com o botão Voltar do Android")
    override fun onBackPressed() {
        back()
    }

    private fun back() {
        if (history.isNotEmpty()) {
            screen = history.removeAt(history.lastIndex)
            render()
        } else if (screen.route != "home") {
            screen = Screen("home")
            render()
        } else {
            finish()
        }
    }

    private fun go(next: Screen, top: Boolean = false) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(root.windowToken, 0)

        if (top) {
            history.clear()
        } else {
            history.add(screen)
        }

        screen = next
        render()
    }

    private fun dp(n: Int): Int {
        return (n * resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun box(
        fill: Int,
        radius: Int = 12,
        stroke: Int = line
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }
    }

    private fun column(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
    }

    private fun row(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun text(
        value: String,
        size: Float = 15f,
        heading: Boolean = false,
        color: Int = ink
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (heading) playfair else lora
            includeFontPadding = false
            setLineSpacing(dp(3).toFloat(), 1f)
        }
    }

    private fun gap(parent: LinearLayout, height: Int) {
        parent.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(height))
        )
    }

    private fun divider(parent: LinearLayout) {
        parent.addView(
            View(this).apply {
                setBackgroundColor(line)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )
    }

    private fun button(
        label: String,
        filled: Boolean = false,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            typeface = Typeface.create(lora, Typeface.BOLD)

            setTextColor(if (filled) cream else ink)

            background = box(
                if (filled) feather else sand,
                12,
                if (filled) feather else line
            )

            minHeight = dp(48)
            minimumHeight = dp(48)
            setPadding(dp(8), dp(10), dp(8), dp(10))

            setOnClickListener {
                action()
            }
        }
    }

    private fun field(
        parent: LinearLayout,
        hintText: String,
        value: String = "",
        numeric: Boolean = false,
        change: (String) -> Unit = {}
    ): EditText {
        val edit = EditText(this).apply {
            hint = hintText
            contentDescription = hintText
            textSize = 14f
            typeface = lora
            setTextColor(ink)
            setHintTextColor(soft)

            background = box(sand)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setSingleLine(true)

            inputType = if (numeric) {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_TEXT
            }

            setText(value)

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    change(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        parent.addView(
            edit,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                bottomMargin = dp(10)
            }
        )

        return edit
    }

    private fun iconButton(
        kind: String,
        description: String,
        action: () -> Unit
    ): FrameLayout {
        return FrameLayout(this).apply {
            contentDescription = description
            isFocusable = true

            setOnClickListener {
                action()
            }

            addView(
                PamIcon(this@MainActivity, kind, feather),
                FrameLayout.LayoutParams(
                    dp(24),
                    dp(24),
                    Gravity.CENTER
                )
            )

            layoutParams = LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        }
    }

    private fun header(title: String) {
        val r = row()

        r.addView(
            iconButton("back", "Voltar") {
                back()
            }
        )

        r.addView(
            text(title, 21f, true).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        r.addView(
            View(this),
            LinearLayout.LayoutParams(dp(48), 1)
        )

        body.addView(r)
        gap(body, 18)
    }

    private fun render() {
        generation++

        root = column().apply {
            setBackgroundColor(cream)
        }

        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            insets
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        body = column().apply {
            setPadding(dp(24), dp(20), dp(24), dp(24))
        }

        scroll.addView(body)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        navigation()

        setContentView(root)
        root.requestApplyInsets()

        when (screen.route) {
            "home" -> home()

            "authors" -> authors()

            "author" -> author()

            "poems" -> search()

            "results" -> {
                header("Resultados")

                val holder = column()
                body.addView(holder)

                request(
                    holder,
                    { api.search(screen.filter) }
                ) {
                    poemList(holder, it)
                }
            }

            "random" -> random()

            "randomResults" -> {
                header("Poemas encontrados")

                val holder = column()
                body.addView(holder)

                val current = screen
                val cached = current.poems

                if (cached != null) {
                    poemList(holder, cached)
                } else {
                    request(
                        holder,
                        { api.random(current.amount) }
                    ) {
                        current.poems = it
                        poemList(holder, it)
                    }
                }
            }

            "poem" -> poem()
        }
    }

    private fun navigation() {
        divider(root)

        val nav = row()

        val tabs = listOf(
            "home" to "Início",
            "authors" to "Autores",
            "poems" to "Poemas",
            "random" to "Aleatórios"
        )

        tabs.forEach { (route, label) ->
            val active = screen.tab == route

            val cell = column().apply {
                gravity = Gravity.CENTER_HORIZONTAL
                isFocusable = true
                contentDescription = label
                isSelected = active

                setOnClickListener {
                    go(Screen(route), true)
                }
            }

            // Indicador superior da aba selecionada.
            cell.addView(
                View(this).apply {
                    setBackgroundColor(
                        if (active) feather else cream
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(3)
                )
            )

            gap(cell, 12)

            // Ícone centralizado.
            cell.addView(
                PamIcon(
                    this,
                    route,
                    if (active) ink else soft
                ),
                LinearLayout.LayoutParams(
                    dp(24),
                    dp(24)
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            )

            gap(cell, 4)

            // Nome centralizado abaixo do ícone.
            cell.addView(
                text(
                    label,
                    13f,
                    false,
                    if (active) ink else soft
                ).apply {
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            // Todas as abas ocupam a mesma largura.
            nav.addView(
                cell,
                LinearLayout.LayoutParams(
                    0,
                    dp(72),
                    1f
                )
            )
        }

        root.addView(nav)
    }

    private fun logo(parent: LinearLayout, size: Int = 72) {
        parent.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.ic_pam)
                contentDescription = "PAM — Poesia, sempre por perto"
            },
            LinearLayout.LayoutParams(
                dp(size),
                dp(size)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
    }

    private fun home() {
        gap(body, 24)
        logo(body)

        body.addView(
            text("PAM", 42f, true, feather).apply {
                gravity = Gravity.CENTER
                setTypeface(playfair, Typeface.ITALIC)
            }
        )

        body.addView(
            text(
                "Poesia, sempre por perto.",
                14f,
                false,
                soft
            ).apply {
                gravity = Gravity.CENTER
                setTypeface(lora, Typeface.ITALIC)
            }
        )

        gap(body, 30)

        val title = field(body, "Buscar por título...")

        title.imeOptions =
            android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH

        title.setOnEditorActionListener { _, _, _ ->
            quickSearch(title)
            true
        }

        body.addView(
            button("Buscar") {
                quickSearch(title)
            }
        )

        gap(body, 18)

        val actions = row()

        listOf(
            "authors" to "Autores",
            "poems" to "Poemas",
            "random" to "Aleatórios"
        ).forEachIndexed { index, (route, label) ->
            actions.addView(
                button(label) {
                    go(Screen(route))
                },
                LinearLayout.LayoutParams(
                    0,
                    dp(80),
                    1f
                ).apply {
                    if (index > 0) {
                        leftMargin = dp(10)
                    }
                }
            )
        }

        body.addView(actions)
        gap(body, 18)

        val card = column().apply {
            background = box(paper, 22, paper)
            setPadding(dp(20), dp(22), dp(20), dp(22))
        }

        body.addView(card)

        fun show(p: Poem?) {
            card.addView(
                text("Poemas aleatórios   ›", 22f, true).apply {
                    setTypeface(playfair, Typeface.ITALIC)
                }
            )

            gap(card, 14)

            if (p == null) {
                card.addView(text("Nenhum poema disponível."))
                return
            }

            card.addView(
                text(
                    p.lines.orEmpty().take(2).joinToString("\n") + "…",
                    17f
                ).apply {
                    setTypeface(lora, Typeface.ITALIC)
                }
            )

            gap(card, 8)

            card.addView(
                text(
                    "${p.author} · ${p.count ?: 0} versos",
                    12f
                )
            )

            card.setOnClickListener {
                open(p)
            }
        }

        if (featured != null) {
            show(featured)
        } else {
            request(
                card,
                { api.featured() }
            ) {
                featured = it
                show(it)
            }
        }
    }

    private fun quickSearch(edit: EditText) {
        if (edit.text.isBlank()) {
            edit.error = "Digite parte do título."
            return
        }

        go(
            Screen(
                "results",
                tab = "poems",
                filter = Filters(
                    title = edit.text.toString()
                )
            )
        )
    }

    private fun authors() {
        header("Autores")

        val holder = column()
        var all = listOf<String>()

        field(
            body,
            "Pesquisar autor...",
            authorQuery
        ) { query ->
            authorQuery = query

            showAuthors(
                holder,
                all.filter { name ->
                    name.contains(query, true)
                }
            )
        }

        body.addView(holder)

        request(
            holder,
            { api.authors() }
        ) { names ->
            all = names

            showAuthors(
                holder,
                names.filter { name ->
                    name.contains(authorQuery, true)
                }
            )
        }
    }

    private fun initials(name: String): String {
        return name.split(" ")
            .filter { it.isNotBlank() }
            .let { words ->
                if (words.size > 1) {
                    "${words.first().first()}${words.last().first()}"
                } else {
                    words.firstOrNull()?.take(2).orEmpty()
                }
            }
    }

    private fun showAuthors(
        holder: LinearLayout,
        names: List<String>
    ) {
        holder.removeAllViews()

        if (names.isEmpty()) {
            holder.addView(
                text(
                    "Nenhum autor encontrado.",
                    15f,
                    false,
                    soft
                )
            )
        }

        names.forEach { name ->
            val r = row().apply {
                setPadding(0, dp(12), 0, dp(12))
                minimumHeight = dp(60)
                isFocusable = true

                setOnClickListener {
                    go(
                        Screen(
                            "author",
                            name,
                            tab = "authors"
                        )
                    )
                }
            }

            r.addView(
                text(initials(name), 12f).apply {
                    gravity = Gravity.CENTER
                    background = box(line, 24, line)
                },
                LinearLayout.LayoutParams(
                    dp(38),
                    dp(38)
                )
            )

            r.addView(
                text(name, 14f),
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    leftMargin = dp(12)
                }
            )

            r.addView(
                PamIcon(this, "next", soft),
                LinearLayout.LayoutParams(
                    dp(18),
                    dp(18)
                )
            )

            holder.addView(r)
            divider(holder)
        }
    }

    private fun author() {
        header("")

        body.addView(
            text(
                initials(screen.name),
                64f,
                true,
                feather
            ).apply {
                gravity = Gravity.CENTER
                background = box(paper, 16, paper)
                setTypeface(playfair, Typeface.ITALIC)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(210)
            )
        )

        gap(body, 18)
        body.addView(text(screen.name, 28f, true))
        gap(body, 8)

        val holder = column()
        body.addView(holder)

        request(
            holder,
            { api.author(screen.name) }
        ) { (bio, poems) ->
            if (bio != null) {
                biography(holder, bio)
            } else {
                holder.addView(
                    text(
                        "Biografia indisponível no momento.",
                        14f,
                        false,
                        soft
                    )
                )

                gap(holder, 12)
            }

            holder.addView(
                text("Poemas no acervo: ${poems.size}", 14f)
            )

            gap(holder, 12)
            holder.addView(text("Poemas deste autor", 17f, true))
            gap(holder, 6)

            poemList(holder, poems)
        }
    }

    private fun translated(value: String): String {
        val translations = mapOf(
            "American" to "Americana",
            "English" to "Inglesa",
            "British" to "Britânica",
            "Scottish" to "Escocesa",
            "Irish" to "Irlandesa",
            "Australian" to "Australiana",
            "19th century" to "Século XIX",
            "20th century" to "Século XX",
            "18th century" to "Século XVIII",
            "death" to "Morte",
            "nature" to "Natureza",
            "love" to "Amor",
            "faith and religion" to "Fé e religião",
            "pain and grief" to "Dor e luto"
        )

        return translations[value] ?: value
    }

    private fun biography(
        parent: LinearLayout,
        bio: JSONObject
    ) {
        val years = listOf(
            bio.optString("born"),
            bio.optString("died")
        ).filter {
            it.isNotBlank()
        }.joinToString(" – ")

        if (years.isNotEmpty()) {
            parent.addView(text(years, 13f, false, soft))
            gap(parent, 12)
        }

        listOf("nationality", "period")
            .map { bio.optString(it) }
            .filter { it.isNotBlank() }
            .forEach {
                chip(parent, translated(it))
            }

        val themes = bio.optJSONArray("themes")

        if (themes != null && themes.length() > 0) {
            gap(parent, 10)
            parent.addView(text("Temas recorrentes", 15f, true))
            gap(parent, 8)

            for (index in 0 until themes.length()) {
                chip(
                    parent,
                    translated(themes.getString(index))
                )
            }
        }

        gap(parent, 14)
    }

    private fun chip(
        parent: LinearLayout,
        label: String
    ) {
        parent.addView(
            text(label, 13f).apply {
                background = box(cream, 18, feather)
                setPadding(dp(12), dp(5), dp(12), dp(5))
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(6)
            }
        )
    }

    private fun search() {
        header("Poemas")

        field(
            body,
            "Buscar por título...",
            filters.title
        ) {
            filters.title = it
        }

        field(
            body,
            "Buscar por autor...",
            filters.author
        ) {
            filters.author = it
        }

        field(
            body,
            "Buscar trecho...",
            filters.lines
        ) {
            filters.lines = it
        }

        gap(body, 8)
        body.addView(text("Filtrar por versos:", 14f))
        gap(body, 8)

        val range = row()
        val minimum = column()
        val maximum = column()

        range.addView(
            minimum,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        range.addView(
            maximum,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(12)
            }
        )

        body.addView(range)

        field(
            minimum,
            "Mín.",
            filters.min,
            true
        ) {
            filters.min = it
        }

        field(
            maximum,
            "Máx.",
            filters.max,
            true
        ) {
            filters.max = it
        }

        body.addView(
            button("Buscar poemas", true) {
                val error = filters.validate()

                if (error != null) {
                    Toast.makeText(
                        this,
                        error,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    go(
                        Screen(
                            "results",
                            tab = "poems",
                            filter = filters.copy()
                        )
                    )
                }
            }
        )

        gap(body, 10)

        body.addView(
            button("Todos os títulos") {
                filters = Filters()

                go(
                    Screen(
                        "results",
                        tab = "poems"
                    )
                )
            }
        )

        gap(body, 22)

        val holder = column()
        body.addView(holder)

        request(
            holder,
            { api.titles() }
        ) {
            poemList(holder, it)
        }
    }

    private fun random() {
        header("Poemas aleatórios")
        gap(body, 88)

        body.addView(
            text(
                "Quantos poemas deseja ver?",
                23f,
                true
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        gap(body, 36)

        val r = row().apply {
            gravity = Gravity.CENTER
        }

        val value = text(
            quantity.toString(),
            42f,
            true
        ).apply {
            gravity = Gravity.CENTER
            contentDescription = "Quantidade de poemas: $quantity"
        }

        val minus = button("−") {
            if (quantity > 1) {
                quantity--
            }

            value.text = quantity.toString()
            value.contentDescription = "Quantidade de poemas: $quantity"
        }

        val plus = button("+") {
            if (quantity < 100) {
                quantity++
            }

            value.text = quantity.toString()
            value.contentDescription = "Quantidade de poemas: $quantity"
        }

        minus.contentDescription = "Diminuir quantidade"
        plus.contentDescription = "Aumentar quantidade"

        minus.background = box(cream, 30, feather)
        plus.background = box(cream, 30, feather)

        r.addView(
            minus,
            LinearLayout.LayoutParams(
                dp(52),
                dp(52)
            )
        )

        r.addView(
            value,
            LinearLayout.LayoutParams(
                dp(110),
                dp(72)
            )
        )

        r.addView(
            plus,
            LinearLayout.LayoutParams(
                dp(52),
                dp(52)
            )
        )

        body.addView(r)
        gap(body, 14)

        body.addView(
            text(
                "De 1 a 100 poemas",
                12f,
                false,
                soft
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        gap(body, 34)

        body.addView(
            button("Gerar poemas", true) {
                go(
                    Screen(
                        "randomResults",
                        tab = "random",
                        amount = quantity
                    )
                )
            }
        )
    }

    private fun open(poem: Poem) {
        go(
            Screen(
                "poem",
                poem.title,
                poem.author,
                screen.tab
            )
        )
    }

    private fun poemList(
        parent: LinearLayout,
        poems: List<Poem>
    ) {
        if (poems.isEmpty()) {
            parent.addView(
                text(
                    "Nenhum poema encontrado.",
                    15f,
                    false,
                    soft
                )
            )

            return
        }

        // Paginação local para evitar milhares de Views simultâneas.
        var offset = 0
        val more = button("Carregar mais") {}

        fun append() {
            parent.removeView(more)

            poems.drop(offset).take(40).forEach { poem ->
                val r = row().apply {
                    setPadding(0, dp(16), 0, dp(16))
                    minimumHeight = dp(56)
                    isFocusable = true

                    setOnClickListener {
                        open(poem)
                    }
                }

                val labels = column()

                labels.addView(
                    text(poem.title, 18f, true)
                )

                gap(labels, 5)

                if (poem.author.isNotBlank()) {
                    labels.addView(
                        text(
                            "${poem.author} · ${poem.count ?: 0} versos",
                            12f,
                            false,
                            soft
                        )
                    )
                }

                r.addView(
                    labels,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                r.addView(
                    PamIcon(this, "next", soft),
                    LinearLayout.LayoutParams(
                        dp(18),
                        dp(18)
                    )
                )

                parent.addView(r)
                divider(parent)
            }

            offset += 40

            if (offset < poems.size) {
                more.text =
                    "Carregar mais (${minOf(offset, poems.size)}/${poems.size})"

                parent.addView(more)
            }
        }

        more.setOnClickListener {
            append()
        }

        append()
    }

    private fun poem() {
        val bar = row()

        bar.addView(
            iconButton("back", "Voltar") {
                back()
            }
        )

        bar.addView(
            View(this),
            LinearLayout.LayoutParams(
                0,
                1,
                1f
            )
        )

        var loaded: Poem? = null

        val share = iconButton(
            "share",
            "Compartilhar poema"
        ) {
            loaded?.let { poem ->
                val content =
                    "${poem.title}\n" +
                            "${poem.author}\n\n" +
                            poem.lines.orEmpty().joinToString("\n") +
                            "\n\nPAM — Poesia, sempre por perto.\n" +
                            "Fonte: PoetryDB"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                }

                startActivity(
                    Intent.createChooser(
                        intent,
                        "Compartilhar poema"
                    )
                )
            }
        }

        share.isEnabled = false
        bar.addView(share)
        body.addView(bar)

        gap(body, 26)

        val holder = column()
        body.addView(holder)

        request(
            holder,
            { api.detail(screen.name, screen.author) }
        ) { poems ->
            if (poems.isEmpty()) {
                holder.addView(
                    text("Poema não encontrado.")
                )
            } else if (poems.size > 1) {
                holder.addView(
                    text(
                        "Escolha o autor deste título:",
                        18f,
                        true
                    )
                )

                poemList(holder, poems)
            } else {
                val poem = poems.first()

                loaded = poem
                share.isEnabled = true

                holder.addView(
                    text(poem.title, 32f, true).apply {
                        gravity = Gravity.CENTER
                    }
                )

                gap(holder, 12)

                holder.addView(
                    text(poem.author, 17f).apply {
                        gravity = Gravity.CENTER
                    }
                )

                gap(holder, 5)

                holder.addView(
                    text(
                        "${poem.count ?: 0} versos",
                        12f,
                        false,
                        soft
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                )

                gap(holder, 24)
                divider(holder)
                gap(holder, 26)

                holder.addView(
                    text(
                        poem.lines.orEmpty().joinToString("\n"),
                        21f
                    ).apply {
                        setTypeface(lora, Typeface.ITALIC)
                        setLineSpacing(dp(8).toFloat(), 1f)
                        setTextIsSelectable(true)
                    }
                )

                gap(holder, 60)
                logo(holder, 86)
            }
        }
    }

    private fun <T> request(
        holder: LinearLayout,
        task: () -> T,
        success: (T) -> Unit
    ) {
        val token = generation

        holder.removeAllViews()

        holder.addView(
            ProgressBar(this).apply {
                indeterminateTintList =
                    ColorStateList.valueOf(feather)

                contentDescription = "Carregando"
            },
            LinearLayout.LayoutParams(
                dp(36),
                dp(36)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        worker.execute {
            val result = runCatching {
                task()
            }

            runOnUiThread {
                if (
                    token != generation ||
                    isFinishing ||
                    isDestroyed
                ) {
                    return@runOnUiThread
                }

                holder.removeAllViews()

                result.fold(success) { error ->
                    val message = if (error is IllegalArgumentException) {
                        error.message.orEmpty()
                    } else {
                        "Não foi possível carregar. " +
                                "Verifique sua conexão e tente novamente."
                    }

                    holder.addView(
                        text(
                            message,
                            15f,
                            false,
                            soft
                        )
                    )

                    gap(holder, 12)

                    holder.addView(
                        button("Tentar novamente") {
                            request(holder, task, success)
                        }
                    )
                }
            }
        }
    }
}