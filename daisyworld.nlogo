globals [
  max-age               ;; 雏菊的最长寿命maximum age that all daisies live to
  global-temperature    ;; 平均全局温度the average temperature of the patches in the world 
  num-blacks            ;; 黑雏菊的数量the number of black daisies
  num-whites            ;; 白雏菊的数量 the number of white daisies
  scenario-phase        ;; 表示太阳日照处于哪种模式下interval counter used to keep track of what portion of scenario is currently occurring
  ]

breed [daisies daisy]   ;;定义一种daisy

patches-own [temperature]  ;;每一个patch都有一个temperature属性 local temperature at this location

;;每个daisy都有的属性
daisies-own [
  age       ;; age of the daisy
  albedo    ;; fraction (0-1) of energy absorbed as heat from sunlight
]


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;随机选择播撒种子，计算全局温度
to setup
  clear-all
  set-default-shape daisies "flower"
  ask patches [ set pcolor gray ];;默认全局patch是灰色

  set max-age 25 ;;最长寿命为25
  set global-temperature 0;;全局温度为0

  ;;根据scenario的不同来设置sun的发光度
  if (scenario = "ramp-up-ramp-down"    ) [ set solar-luminosity 0.8 ]
  if (scenario = "low solar luminosity" ) [ set solar-luminosity 0.6 ]
  if (scenario = "our solar luminosity" ) [ set solar-luminosity 1.0 ]
  if (scenario = "high solar luminosity") [ set solar-luminosity 1.4 ]

  seed-blacks-randomly
  seed-whites-randomly
  ask daisies [set age random max-age]
  ask patches [calc-temperature]
  set global-temperature (mean [temperature] of patches)
  update-display
  reset-ticks
end

;;根据黑色daisy的数量，四舍五入随机选择一些patchs来放seed
to seed-blacks-randomly
   ask n-of round ((start-%-blacks * count patches) / 100) patches with [not any? daisies-here]
     [ sprout-daisies 1 [set-as-black] ]
end

;;根据白色daisy的数量，floor随机选择一些patchs来放seed
to seed-whites-randomly
   ask n-of floor ((start-%-whites * count patches) / 100) patches with [not any? daisies-here]
     [ sprout-daisies 1 [set-as-white] ]
end


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Runtime Procedures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;每一个go进行的操作是：1. 计算当前所有patch的温度 2. 散热 3. 检查daisy的存活程度，并进行周边的繁殖 4. 更新显示 5. 增加tick
;; 6. 根据scenario的不同来更新sun的发光度

to go
   ask patches [calc-temperature];;计算每个patch的当前温度
   diffuse temperature .5 ;;diffuse是一个primary操作，表示将后面的variable消散0.5给它的8个邻居，每个邻居获得1/8
   ask daisies [check-survivability]
   set global-temperature (mean [temperature] of patches)
   update-display
   tick
   if scenario = "ramp-up-ramp-down" [
     if ticks > 200 and ticks <= 400 [
       set solar-luminosity precision (solar-luminosity + 0.005) 4 ;;precision：有效数字，或者精确度
     ]
     if ticks > 600 and ticks <= 850 [
       set solar-luminosity precision (solar-luminosity - 0.0025) 4
     ]
   ]
   if scenario = "low solar luminosity"  [ set solar-luminosity 0.6 ]
   if scenario = "our solar luminosity"  [ set solar-luminosity 1.0 ]
   if scenario = "high solar luminosity" [ set solar-luminosity 1.4 ]
end

;;在某个patch出设置一个黑色turtle
to set-as-black ;; turtle procedure
  set color black
  set albedo albedo-of-blacks
  set age 0
  set size 0.6
end

;;在某个patch出设置一个白色turtle
to set-as-white  ;; turtle procedure
  set color white
  set albedo albedo-of-whites
  set age 0
  set size 0.6
end

;;判断daisy是否还可以存活，并在周边进行适当的繁殖
to check-survivability ;; turtle procedure
  ;;定义一组局部变量
  let seed-threshold 0;;该变量表示邻居繁殖出新的daisy的可能性
  let not-empty-spaces nobody;;表示一个agent为空
  let seeding-place nobody

  set age (age + 1)
  ifelse age < max-age
  [
     ;;繁殖的可能性曲线基于daisy所在处的温度 
     set seed-threshold ((0.1457 * temperature) - (0.0032 * (temperature ^ 2)) - 0.6443)
     ;; This equation may look complex, but it is just a parabola.
     ;; This parabola has a peak value of 1 -- the maximum growth factor possible at an optimum
     ;; temperature of 22.5 degrees C
     ;; -- and drops to zero at local temperatures of 5 degrees C and 40 degrees C. [the x-intercepts]
     ;; Thus, growth of new daisies can only occur within this temperature range,
     ;; with decreasing probability of growth new daisies closer to the x-intercepts of the parabolas
     ;; remember, however, that this probability calculation is based on the local temperature.
     ;;可以繁殖
     if (random-float 1.0 < seed-threshold) [
       set seeding-place one-of neighbors with [not any? daisies-here];;在neighbour周边找到一个空的地方

       if (seeding-place != nobody)
       [
         if (color = white)
         [
           ask seeding-place [sprout-daisies 1 [set-as-white]  ]
         ]
         if (color = black)
         [
           ask seeding-place [sprout-daisies 1 [set-as-black]  ]
         ]
       ]
     ]
  ]
  [die]
end

;;计算patch的温度
to calc-temperature  ;; patch procedure
  let absorbed-luminosity 0
  let local-heating 0
  ifelse not any? daisies-here
  ;;如果本模块没有daisy，就计算surface吸收的energy
  [   ;; the percentage of absorbed energy is calculated (1 - albedo-of-surface) and then multiplied by the solar-luminosity
      ;; to give a scaled absorbed-luminosity.
    set absorbed-luminosity ((1 - albedo-of-surface) * solar-luminosity)
  ]
  ;;如果本地有daisy，计算daisy吸收的energy
  [
      ;; the percentage of absorbed energy is calculated (1 - albedo) and then multiplied by the solar-luminosity
      ;; to give a scaled absorbed-luminosity.
    ask one-of daisies-here
      [set absorbed-luminosity ((1 - albedo) * solar-luminosity)]
  ]
  ;; local-heating is calculated as logarithmic（对数） function of solar-luminosity
  ;; where a absorbed-luminosity of 1 yields a local-heating of 80 degrees C
  ;; and an absorbed-luminosity of .5 yields a local-heating of approximately 30 C
  ;; and a absorbed-luminosity of 0.01 yields a local-heating of approximately -273 C
  ;;温度计算公式
  ifelse absorbed-luminosity > 0
      [set local-heating 72 * ln absorbed-luminosity + 80]
      [set local-heating 80]
  set temperature ((temperature + local-heating) / 2)
     ;; set the temperature at this patch to be the average of the current temperature and the local-heating effect
end

to paint-daisies   ;; daisy painting procedure which uses the mouse location draw daisies when the mouse button is down
  if mouse-down?
  [
    ask patch mouse-xcor mouse-ycor [
      ifelse not any? daisies-here
      [
        if paint-daisies-as = "add black"
          [sprout-daisies 1 [set-as-black]]
        if paint-daisies-as = "add white"
          [sprout-daisies 1 [set-as-white]]
      ]
      [
        if paint-daisies-as = "remove"
          [ask daisies-here [die]]
      ]
      display  ;; update view
    ]
  ]
end

;;对显示界面进行更新，是对每一个patch进行操作
to update-display
  ifelse (show-temp-map? = true)
    [ ask patches [set pcolor scale-color red temperature -50 110] ]  ;; scale color of patches to the local temperature
    [ ask patches [set pcolor grey] ]

  ifelse (show-daisies? = true)
    [ ask daisies [set hidden? false] ]
    [ ask daisies [set hidden? true] ]
end


; Copyright 2006 Uri Wilensky.
; See Info tab for full copyright and license.