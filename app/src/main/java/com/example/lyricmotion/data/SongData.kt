package com.lyricmotion.data

import androidx.compose.ui.text.font.FontFamily

// ================================================================
//  ENUMS
// ================================================================

enum class SearchState { IDLE, LOADING, EMPTY, ERROR, RESULTS }
enum class LyricStyle  { NEON, KARAOKE, FADE }

// ================================================================
//  MODELOS
// ================================================================

data class SongItem(
    val id:       String,
    val title:    String,
    val artist:   String,
    val duration: String,
    val lyrics:   String = ""
)

// ================================================================
//  DATOS — letras completas
// ================================================================

val sampleSongs = listOf(
    SongItem("1", "Bohemian Rhapsody", "Queen", "5:55",
        "Is this the real life?\nIs this just fantasy?\nCaught in a landslide\nNo escape from reality\n\nOpen your eyes\nLook up to the skies and see\nI'm just a poor boy, I need no sympathy\nBecause it's easy come, easy go\nLittle high, little low\nAnyway the wind blows doesn't really matter to me, to me\n\nMama, just killed a man\nPut a gun against his head\nPulled my trigger, now he's dead\nMama, life had just begun\nBut now I've gone and thrown it all away\n\nMama, ooh\nDidn't mean to make you cry\nIf I'm not back again this time tomorrow\nCarry on, carry on as if nothing really matters\n\nToo late, my time has come\nSends shivers down my spine\nBody's aching all the time\nGoodbye, everybody, I've got to go\nGotta leave you all behind and face the truth\n\nMama, ooh\nI don't want to die\nI sometimes wish I'd never been born at all\n\nI see a little silhouetto of a man\nScaramouche, Scaramouche, will you do the Fandango?\nThunderbolt and lightning very, very frightening me\nGalileo, Galileo\nGalileo figaro, magnifico\n\nI'm just a poor boy, nobody loves me\nHe's just a poor boy from a poor family\nSpare him his life from this monstrosity\n\nEasy come, easy go, will you let me go?\nBismillah! No, we will not let you go\nLet him go!"),

    SongItem("2", "Imagine", "John Lennon", "3:07",
        "Imagine there's no heaven\nIt's easy if you try\nNo hell below us\nAbove us, only sky\nImagine all the people living for today\n\nImagine there's no countries\nIt isn't hard to do\nNothing to kill or die for\nAnd no religion, too\nImagine all the people living life in peace\n\nYou may say I'm a dreamer\nBut I'm not the only one\nI hope someday you'll join us\nAnd the world will be as one\n\nImagine no possessions\nI wonder if you can\nNo need for greed or hunger\nA brotherhood of man\nImagine all the people sharing all the world\n\nYou may say I'm a dreamer\nBut I'm not the only one\nI hope someday you'll join us\nAnd the world will live as one"),

    SongItem("3", "Starboy", "The Weeknd", "3:50",
        "I'm tryna put you in the worst mood, ah\nP1 cleaner than your church shoes, ah\nMilli point two just to hurt you, ah\nAll red Lamb' just to swerve you, ah\n\nUsed to call me on my cell phone\nLate night when you need my love\nCall me on my cell phone\nLate night when you need my love\n\nLook what you've done\nI'm a starboy\nLook what you've done\nI'm a starboy\n\nEvery day a nigga try to test me\nEvery day a nigga try to test me\nAll my nightmares, they a blessing\nAll my nightmares, they a blessing\n\nHouse so empty, need a centerpiece\nTwenty racks a table cut from ebony\nCut that ivory into skinny pieces\nThen she clean it with her face, man I love my baby"),

    SongItem("4", "Blinding Lights", "The Weeknd", "3:20",
        "I've been on my own for long enough\nMaybe you can show me how to love, maybe\nI'm going through withdrawals\n\nYou don't even have to do too much\nYou can turn me on with just a touch, baby\n\nI look around and sin city's cold and empty\nNo one's around to judge me\nI can't see clearly when you're gone\n\nI said, ooh, I'm blinded by the lights\nNo, I can't sleep until I feel your touch\nI said, ooh, I'm drowning in the night\nOh, when I'm like this, you're the one I trust\n\nI'm running out of time\nCause I can see the sun light up the sky\nSo I hit the road in overdrive, baby\n\nThe city's cold and empty\nNo one's around to judge me\nI can't see clearly when you're gone\n\nI said, ooh, I'm blinded by the lights\nNo, I can't sleep until I feel your touch"),

    SongItem("5", "Shape of You", "Ed Sheeran", "3:53",
        "The club isn't the best place to find a lover\nSo the bar is where I go\nMe and my friends at the table doing shots\nDrinking fast and then we talk slow\n\nCome over and start up a conversation with just me\nAnd trust me I'll give it a chance now\nTake my hand, stop, put Van the Man on the jukebox\nAnd then we start to dance\n\nGirl, you know I want your love\nYour love was handmade for somebody like me\nCome on now, follow my lead\nI may be crazy, don't mind me\n\nI'm in love with the shape of you\nWe push and pull like a magnet do\nAlthough my heart is falling too\nI'm in love with your body\n\nLast night you were in my room\nAnd now my bedsheets smell like you\nEvery day discovering something brand new\nI'm in love with your body"),

    SongItem("6", "Perfect", "Ed Sheeran", "4:23",
        "I found a love for me\nDarling, just dive right in and follow my lead\nWell, I found a girl, beautiful and sweet\nOh, I never knew you were the someone waiting for me\n\nCause we were just kids when we fell in love\nNot knowing what it was\nI will not give you up this time\n\nDarling, just kiss me slow, your heart is all I own\nAnd in your eyes you're holding mine\n\nBaby, I'm dancing in the dark\nWith you between my arms\nBarefoot on the grass\nListening to our favourite song\n\nWhen you said you looked a mess\nI whispered underneath my breath\nBut you heard it, darling, you look perfect tonight\n\nWell I found a woman, stronger than anyone I know\nShe shares my dreams, I hope that someday I'll share her home\nI found a love, to carry more than just my secrets\nTo carry love, to carry children of our own"),

    SongItem("7", "Hallelujah", "Leonard Cohen", "4:36",
        "I've heard there was a secret chord\nThat David played, and it pleased the Lord\nBut you don't really care for music, do you?\nIt goes like this: the fourth, the fifth\nThe minor fall, the major lift\nThe baffled king composing Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah\n\nYour faith was strong but you needed proof\nYou saw her bathing on the roof\nHer beauty in the moonlight overthrew you\nShe tied you to a kitchen chair\nShe broke your throne, and she cut your hair\nAnd from your lips she drew the Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah\n\nMaybe there's a God above\nBut all I've ever learned from love\nWas how to shoot at someone who outdrew you\nIt's not a cry you can hear at night\nIt's not somebody who has seen the light\nIt's a cold and it's a broken Hallelujah\n\nHallelujah, Hallelujah\nHallelujah, Hallelujah"),

    SongItem("8", "Yellow", "Coldplay", "4:29",
        "Look at the stars\nLook how they shine for you\nAnd everything you do\nYeah, they were all yellow\n\nI came along\nI wrote a song for you\nAnd all the things you do\nAnd it was called Yellow\n\nSo then I took my turn\nOh, what a thing to have done\nAnd it was all yellow\n\nYour skin, oh yeah, your skin and bones\nTurn into something beautiful\nDo you know, you know I love you so\nYou know I love you so\n\nI swam across, I jumped across for you\nOh, what a thing to do\nCause you were all yellow\n\nI drew a line, I drew a line for you\nOh, what a thing to do\nAnd it was all yellow\n\nYour skin, oh yeah your skin and bones\nTurn into something beautiful\nAnd you know, for you I'd bleed myself dry\nFor you I'd bleed myself dry\n\nIt's true, look how they shine for you\nLook how they shine for you\nLook how they shine\nLook at the stars, look how they shine for you\nAnd all the things that you do"),

    SongItem("9", "Someone Like You", "Adele", "4:45",
        "I heard that you're settled down\nThat you found a girl and you're married now\nI heard that your dreams came true\nGuess she gave you things I didn't give to you\n\nOld friend, why are you so shy?\nAin't like you to hold back or hide from the light\n\nI hate to turn up out of the blue uninvited\nBut I couldn't stay away, I couldn't fight it\nI had hoped you'd see my face\nAnd that you'd be reminded that for me it isn't over\n\nNever mind, I'll find someone like you\nI wish nothing but the best for you, too\nDon't forget me, I beg, I remember you said\nSometimes it lasts in love, but sometimes it hurts instead\n\nYou know how the time flies\nOnly yesterday was the time of our lives\nWe were born and raised in a summer haze\nBound by the surprise of our glory days\n\nNever mind, I'll find someone like you\nI wish nothing but the best for you, too\nDon't forget me, I beg, I remember you said\nSometimes it lasts in love, but sometimes it hurts instead"),

    SongItem("10", "Rolling in the Deep", "Adele", "3:48",
        "There's a fire starting in my heart\nReaching a fever pitch and it's bringing me out the dark\nFinally, I can see you crystal clear\nGo ahead and sell me out and I'll lay your ship bare\n\nSee how I'll leave with every piece of you\nDon't underestimate the things that I will do\n\nThe scars of your love remind me of us\nThey keep me thinking that we almost had it all\nThe scars of your love, they leave me breathless\nI can't help feeling\n\nWe could have had it all\nRolling in the deep\nYou had my heart inside of your hand\nAnd you played it to the beat\n\nBaby, I have no story to be told\nBut I've heard one on you and I'm gonna make your head burn\nThink of me in the depths of your despair\nMaking a home down there as mine sure won't be shared\n\nWe could have had it all\nRolling in the deep\nYou had my heart inside of your hand\nAnd you played it to the beat\n\nThrow your soul through every open door\nCount your blessings to find what you look for\nTurn my sorrow into treasured gold\nYou pay me back in kind and reap just what you sow")
)

val featuredSongs = sampleSongs.take(3)

// ================================================================
//  FUENTES POR ESTILO
// ================================================================

val fontNeon    = FontFamily.Monospace
val fontKaraoke = FontFamily.Serif
val fontFade    = FontFamily.SansSerif

fun lyricFontFamily(style: LyricStyle) = when (style) {
    LyricStyle.NEON    -> fontNeon
    LyricStyle.KARAOKE -> fontKaraoke
    LyricStyle.FADE    -> fontFade
}
