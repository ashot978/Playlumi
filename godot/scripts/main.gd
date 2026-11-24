extends Control
## Separate safe test app: Godot prototype of the star-catching mini-game.
const ROUNDS := 10
const SKY := Color("#152c64")
const LIGHT := Color("#e9f6ff")
const GOLD := Color("#ffd94f")
const PINK := Color("#ff9ad8")
var age := 0
var round_index := 0
var target_count := 3
var collected_mask := 0
var star_buttons: Array[Button] = []
var counter: Label
var message: Label
var board: Control
var advance_button: Button
var restart_button: Button
var menu: Control
var game: Control
var rng := RandomNumberGenerator.new()

func _ready() -> void:
    rng.randomize()
    _build_ui()
    _show_menu()

func _build_ui() -> void:
    var background := ColorRect.new()
    background.color = SKY
    background.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    add_child(background)
    menu = VBoxContainer.new()
    menu.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    menu.add_theme_constant_override("separation", 28)
    add_child(menu)
    var title := Label.new()
    title.text = "✦ Playlumi ✦"
    title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    title.add_theme_font_size_override("font_size", 70)
    title.add_theme_color_override("font_color", GOLD)
    menu.add_child(title)
    var prompt := Label.new()
    prompt.text = "Kies een niveau"
    prompt.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    prompt.add_theme_color_override("font_color", LIGHT)
    menu.add_child(prompt)
    for level in 3:
        var b := Button.new()
        b.text = "Vang de sterren - niveau %d" % (level + 1)
        b.custom_minimum_size = Vector2(0, 110)
        b.pressed.connect(_start_game.bind(level))
        menu.add_child(b)
    game = VBoxContainer.new()
    game.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    game.add_theme_constant_override("separation", 20)
    add_child(game)
    var back := Button.new()
    back.text = "Terug"
    back.pressed.connect(_show_menu)
    game.add_child(back)
    counter = Label.new()
    counter.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    counter.add_theme_color_override("font_color", GOLD)
    game.add_child(counter)
    message = Label.new()
    message.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    message.add_theme_color_override("font_color", LIGHT)
    game.add_child(message)
    board = Control.new()
    board.size_flags_vertical = Control.SIZE_EXPAND_FILL
    board.clip_contents = true
    game.add_child(board)
    advance_button = Button.new()
    advance_button.text = "Volgende ronde"
    advance_button.custom_minimum_size = Vector2(0, 110)
    advance_button.pressed.connect(_advance)
    game.add_child(advance_button)
    restart_button = Button.new()
    restart_button.text = "Opnieuw spelen"
    restart_button.custom_minimum_size = Vector2(0, 110)
    restart_button.pressed.connect(_restart_game)
    game.add_child(restart_button)

func _restart_game() -> void:
    _start_game(age)

func _show_menu() -> void:
    menu.show()
    game.hide()

func _start_game(selected_age: int) -> void:
    age = clampi(selected_age, 0, 2)
    round_index = 0
    menu.hide()
    game.show()
    _make_round()

func _make_round() -> void:
    for old in board.get_children():
        board.remove_child(old)
        old.queue_free()
    star_buttons.clear()
    collected_mask = 0
    target_count = 3 + age
    var total := target_count + 2 + rng.randi_range(0, 1)
    advance_button.hide()
    restart_button.hide()
    counter.text = "Ronde %d / %d" % [round_index + 1, ROUNDS]
    message.text = "Vang %d sterren!" % target_count
    var grid := GridContainer.new()
    grid.columns = 3
    grid.position = Vector2(15, 60)
    grid.add_theme_constant_override("h_separation", 15)
    grid.add_theme_constant_override("v_separation", 30)
    board.add_child(grid)
    for i in total:
        var star := Button.new()
        star.text = "★"
        star.custom_minimum_size = Vector2(180, 140)
        star.add_theme_font_size_override("font_size", 65)
        star.add_theme_color_override("font_color", GOLD if i % 2 == 0 else PINK)
        star.pressed.connect(_catch_star.bind(i))
        grid.add_child(star)
        star_buttons.append(star)

func _catch_star(index: int) -> void:
    if index < 0 or index >= star_buttons.size() or _caught(index) or _complete():
        return
    collected_mask |= 1 << index
    star_buttons[index].disabled = true
    star_buttons[index].text = "✓"
    message.text = "Gevangen: %d / %d" % [_count_collected(), target_count]
    if _complete():
        message.text = "Goed gedaan!"
        if round_index + 1 == ROUNDS:
            restart_button.show()
        else:
            advance_button.show()

func _advance() -> void:
    if not _complete() or round_index + 1 >= ROUNDS:
        return
    round_index += 1
    _make_round()

func _caught(index: int) -> bool:
    return (collected_mask & (1 << index)) != 0

func _count_collected() -> int:
    var count := 0
    var bits := collected_mask
    while bits != 0:
        count += bits & 1
        bits >>= 1
    return count

func _complete() -> bool:
    return _count_collected() == target_count
