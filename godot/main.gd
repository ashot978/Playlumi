extends Control

const CountingGame = preload("res://counting_game.gd")
var game = CountingGame.new()
var objects: Label
var feedback: Label
var choices: HBoxContainer

func _ready() -> void:
	var background := ColorRect.new()
	background.color = Color("#ddf2e4")
	background.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(background)
	var margin := MarginContainer.new()
	margin.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	for edge in ["margin_left", "margin_right", "margin_top", "margin_bottom"]:
		margin.add_theme_constant_override(edge, 24)
	add_child(margin)
	var layout := VBoxContainer.new()
	layout.add_theme_constant_override("separation", 24)
	margin.add_child(layout)
	var title := Label.new()
	title.text = "Playlumi"
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title.add_theme_font_size_override("font_size", 42)
	title.add_theme_color_override("font_color", Color("#254e3c"))
	layout.add_child(title)
	var instruction := Label.new()
	instruction.text = "Hoeveel sterren zie je?"
	instruction.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	instruction.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	instruction.add_theme_font_size_override("font_size", 30)
	instruction.add_theme_color_override("font_color", Color("#254e3c"))
	layout.add_child(instruction)
	objects = Label.new()
	objects.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	objects.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	objects.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	objects.size_flags_vertical = Control.SIZE_EXPAND_FILL
	objects.add_theme_font_size_override("font_size", 64)
	objects.add_theme_color_override("font_color", Color("#9a6411"))
	layout.add_child(objects)
	feedback = Label.new()
	feedback.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	feedback.custom_minimum_size.y = 55
	feedback.add_theme_font_size_override("font_size", 26)
	feedback.add_theme_color_override("font_color", Color("#254e3c"))
	layout.add_child(feedback)
	choices = HBoxContainer.new()
	choices.alignment = BoxContainer.ALIGNMENT_CENTER
	choices.add_theme_constant_override("separation", 16)
	layout.add_child(choices)
	for number in range(1, 4):
		var button := Button.new()
		button.text = str(number)
		button.custom_minimum_size = Vector2(120, 100)
		button.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		button.add_theme_font_size_override("font_size", 36)
		button.pressed.connect(_choose.bind(number))
		choices.add_child(button)
	var again := Button.new()
	again.text = "Nog een ronde"
	again.custom_minimum_size.y = 100
	again.add_theme_font_size_override("font_size", 26)
	again.pressed.connect(_next_round)
	layout.add_child(again)
	game.set_maximum(3)
	_show_round()

func _show_round() -> void:
	var stars: Array[String] = []
	for i in game.answer:
		stars.append("★")
	objects.text = "  ".join(stars)
	feedback.text = ""

func _choose(number: int) -> void:
	if game.solved:
		return
	if game.choose(number):
		feedback.text = "Goed geteld!"
	else:
		feedback.text = "Probeer het nog eens."

func _next_round() -> void:
	game.next_round()
	_show_round()
