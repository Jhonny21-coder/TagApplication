function captureImageProperties() {
    canvas.on('object:modified', function(event) {
        var activeObject = event.target;
        if (activeObject && activeObject.type === 'image') {
            var left = activeObject.left;
            var top = activeObject.top;
            var width = activeObject.width * activeObject.scaleX;
            var height = activeObject.height * activeObject.scaleY;
            var angle = activeObject.angle;

            // Send data to the backend via Vaadin
            $0.$server.saveImageProperties(left, top, width, height, angle);
        }
    });
}
